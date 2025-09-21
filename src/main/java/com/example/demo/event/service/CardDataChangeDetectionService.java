package com.example.demo.event.service;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.example.demo.benefit.application.dto.ChannelType;
import com.example.demo.benefit.entity.CashbackBenefit;
import com.example.demo.benefit.entity.DiscountBenefit;
import com.example.demo.benefit.entity.PointBenefit;
import com.example.demo.card.entity.CardBenefit;
import com.example.demo.card.repository.CardBenefitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.benefit.entity.Benefit;
import com.example.demo.benefit.repository.BenefitRepository;
import com.example.demo.card.entity.Card;
import com.example.demo.card.repository.CardRepository;
import com.sub.grpc.CardData;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardDataChangeDetectionService {

    private final CardBenefitRepository cardBenefitRepository;
    private final BenefitRepository benefitRepository;
    private final CardRepository cardRepository;

    /**
     * 크롤링된 카드 데이터 처리 및 변경 감지
     */
    @Transactional
    public void processCardDataChanges(Object crawledData) {
        log.info("크롤링된 카드 데이터 처리 시작");
        
        try {
            // gRPC 데이터 타입 확인 및 처리
            if (crawledData instanceof CardData.CrawledBenefitList) {
                processCrawledBenefitList((CardData.CrawledBenefitList) crawledData);
            } else {
                log.warn("지원하지 않는 데이터 타입: {}", crawledData.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log.error("크롤링된 카드 데이터 처리 중 오류 발생", e);
        }
        
        log.info("크롤링된 카드 데이터 처리 완료");
    }

    /**
     * CrawledBenefitList 데이터 처리
     */
    @Transactional
    public void processCrawledBenefitList(CardData.CrawledBenefitList crawledBenefitList) {
        int updateCount = 0;
        int createCount = 0;

        for (CardData.CrawledBenefit crawledCard : crawledBenefitList.getCrawledBenefitList()) {
            try {
                // 1. 카드 조회 또는 생성
                Card card = cardRepository.findByExternalId(crawledCard.getCardId())
                        .map(existingCard -> updateCardIfNeeded(existingCard, crawledCard))
                        .orElseGet(() -> createCard(crawledCard));

                // 2. 카드-혜택 매핑 동기화
                boolean changed = syncCardBenefits(card, crawledCard.getBenefitsList());

                if (changed) {
                    updateCount++;
                    log.info("변경된 카드 저장 완료: id={}, name={}", card.getId(), card.getCardName());
                } else {
                    createCount++;
                }
            } catch (Exception e) {
                log.error("카드 동기화 실패: 카드사 {}, 이름 {}", crawledCard.getCardBank(), crawledCard.getCardName(), e);
            }
        }

        log.info("동기화 완료 - 변경 카드: {}, 신규 카드: {}", updateCount, createCount);
    }

    private Card updateCardIfNeeded(Card card, CardData.CrawledBenefit crawledCard) {
        boolean changed = false;

        if (!crawledCard.getCardName().equals(card.getCardName())) {
            card = card.builder().cardName(crawledCard.getCardName()).build();
            changed = true;
        }
        if (!crawledCard.getCardBank().equals(card.getCardBank())) {
            card = card.builder().cardBank(crawledCard.getCardBank()).build();
            changed = true;
        }
        if (!crawledCard.getImgUrl().equals(card.getImgUrl())) {
            card = card.builder().imgUrl(crawledCard.getImgUrl()).build();
            changed = true;
        }
        if (!crawledCard.getType().equals(card.getType())) {
            card = card.builder().type(crawledCard.getType()).build();
            changed = true;
        }

        if (changed) {
            return cardRepository.save(card);
        }
        return card;
    }

    private Card createCard(CardData.CrawledBenefit crawledCard) {
        Card card = Card.builder()
                .cardName(crawledCard.getCardName())
                .cardBank(crawledCard.getCardBank())
                .imgUrl(crawledCard.getImgUrl())
                .type(crawledCard.getType())
                .externalId(crawledCard.getCardId())
                .build();
        return cardRepository.save(card);
    }

    private boolean syncCardBenefits(Card card, List<CardData.Benefit> benefitList) {
        boolean hasChanges = false;

        // 현재 DB에 저장된 카드-혜택 관계 조회
        List<CardBenefit> existingCardBenefits = cardBenefitRepository.findByCard(card);

        Map<Long, CardBenefit> existingBenefitMap = existingCardBenefits.stream()
                .collect(Collectors.toMap(cb -> cb.getBenefit().getExternalId(), cb -> cb));

        for (CardData.Benefit protoBenefit : benefitList) {
            Long benefitExtId = protoBenefit.getBenefitId();

            Optional<CardBenefit> cardBenefitOpt = Optional.ofNullable(existingBenefitMap.get(benefitExtId));
            if (cardBenefitOpt.isPresent()) {

                // 기존 혜택 업데이트 및 저장 여부 판단
                CardBenefit cardBenefit = cardBenefitOpt.get();
                Benefit benefit = cardBenefit.getBenefit();
                boolean benefitChanged = updateBenefitIfNeeded(benefit, protoBenefit);
                if (benefitChanged) {
                    benefitRepository.save(benefit);
                    hasChanges = true;
                }
                existingBenefitMap.remove(benefitExtId);
            } else {
                // 신규 혜택 및 관계 생성
                Benefit newBenefit = findOrCreateBenefit(protoBenefit);
                CardBenefit newCardBenefit = CardBenefit.builder()
                        .card(card)
                        .benefit(newBenefit)
                        .build();
                cardBenefitRepository.save(newCardBenefit);
                hasChanges = true;
            }
        }

        return hasChanges;
    }

    private boolean updateBenefitIfNeeded(Benefit benefit, CardData.Benefit protoBenefit) {
        return updateBenefitDetails(benefit, protoBenefit);
    }


    private Benefit findOrCreateBenefit(CardData.Benefit protoBenefit) {
        return benefitRepository.findByExternalId(protoBenefit.getBenefitId())
                .map(existingBenefit -> updateDiscountBenefits(existingBenefit, protoBenefit.getDiscountsList())
                        ? benefitRepository.save(existingBenefit)
                        : existingBenefit)
                .orElseGet(() -> createBenefit(protoBenefit));
    }

    private boolean updateDiscountBenefits(Benefit benefit, List<CardData.DiscountBenefit> protoDiscounts) {
        AtomicBoolean hasChanges = new AtomicBoolean(false);

        // 기존 디스카운트 혜택 맵핑 (externalId -> DiscountBenefit)
        Map<Long, DiscountBenefit> existingMap = benefit.getDiscountBenefits().stream()
                .collect(Collectors.toMap(DiscountBenefit::getExternalId, d -> d));

        for (CardData.DiscountBenefit proto : protoDiscounts) {
            Long extId = proto.getId();
            DiscountBenefit dbDiscount = existingMap.get(extId);

            if (dbDiscount == null) {
                DiscountBenefit newDiscount = DiscountBenefit.builder()
                        .benefit(benefit)
                        .externalId(extId)
                        .rate(proto.getRate())
                        .amount(proto.getAmount())
                        .minimumAmount(proto.getMinimumAmount())
                        .benefitLimit(proto.getBenefitLimit())
                        .channel(ChannelType.valueOf(proto.getChannel().name()))
                        .build();
                benefit.getDiscountBenefits().add(newDiscount);
                hasChanges.set(true);
            } else {
                if (!Objects.equals(dbDiscount.getRate(), proto.getRate())) {
                    dbDiscount.setRate(proto.getRate()); hasChanges.set(true);
                }
                if (!Objects.equals(dbDiscount.getAmount(), proto.getAmount())) {
                    dbDiscount.setAmount(proto.getAmount()); hasChanges.set(true);
                }
                if (!Objects.equals(dbDiscount.getMinimumAmount(), proto.getMinimumAmount())) {
                    dbDiscount.setMinimumAmount(proto.getMinimumAmount()); hasChanges.set(true);
                }
                if (!Objects.equals(dbDiscount.getBenefitLimit(), proto.getBenefitLimit())) {
                    dbDiscount.setBenefitLimit(proto.getBenefitLimit()); hasChanges.set(true);
                }
                if (dbDiscount.getChannel() != ChannelType.valueOf(proto.getChannel().name())) {
                    dbDiscount.setChannel(ChannelType.valueOf(proto.getChannel().name())); hasChanges.set(true);
                }
            }
            existingMap.remove(extId);
        }

        existingMap.values().forEach(dbDiscount -> {
            benefit.getDiscountBenefits().remove(dbDiscount);
            hasChanges.set(true);
        });

        return hasChanges.get();
    }

    private boolean updatePointBenefits(Benefit benefit, List<CardData.PointBenefit> protoPoints) {
        AtomicBoolean hasChanges = new AtomicBoolean(false);

        Map<Long, PointBenefit> existingMap = benefit.getPointBenefits().stream()
                .collect(Collectors.toMap(PointBenefit::getExternalId, d -> d));

        for (CardData.PointBenefit proto : protoPoints) {
            Long extId = proto.getId();
            PointBenefit dbPoint = existingMap.get(extId);

            if (dbPoint == null) {
                PointBenefit newPointBenefit = PointBenefit.builder()
                        .benefit(benefit)
                        .externalId(extId)
                        .rate(proto.getRate())
                        .minimumAmount(proto.getMinimumAmount())
                        .benefitLimit(proto.getBenefitLimit())
                        .channel(ChannelType.valueOf(proto.getChannel().name()))
                        .build();
                benefit.getPointBenefits().add(newPointBenefit);
                hasChanges.set(true);
            } else {
                if (!Objects.equals(dbPoint.getRate(), proto.getRate())) {
                    dbPoint.setRate(proto.getRate()); hasChanges.set(true);
                }
                if (!Objects.equals(dbPoint.getMinimumAmount(), proto.getMinimumAmount())) {
                    dbPoint.setMinimumAmount(proto.getMinimumAmount()); hasChanges.set(true);
                }
                if (!Objects.equals(dbPoint.getBenefitLimit(), proto.getBenefitLimit())) {
                    dbPoint.setBenefitLimit(proto.getBenefitLimit()); hasChanges.set(true);
                }
                if (dbPoint.getChannel() != ChannelType.valueOf(proto.getChannel().name())) {
                    dbPoint.setChannel(ChannelType.valueOf(proto.getChannel().name())); hasChanges.set(true);
                }
            }
            existingMap.remove(extId);
        }

        existingMap.values().forEach(dbDiscount -> {
            benefit.getDiscountBenefits().remove(dbDiscount);
            hasChanges.set(true);
        });

        return hasChanges.get();
    }

    private boolean updateCashbackBenefits(Benefit benefit, List<CardData.CashbackBenefit> protoCashbacks) {
        AtomicBoolean hasChanges = new AtomicBoolean(false);

        Map<Long, CashbackBenefit> existingMap = benefit.getCashbackBenefits().stream()
                .collect(Collectors.toMap(CashbackBenefit::getExternalId, d -> d));

        for (CardData.CashbackBenefit proto : protoCashbacks) {
            Long extId = proto.getId();
            CashbackBenefit dbCashback = existingMap.get(extId);

            if (dbCashback == null) {
                CashbackBenefit newCashbackBenefit = CashbackBenefit.builder()
                        .benefit(benefit)
                        .externalId(extId)
                        .rate(proto.getRate())
                        .minimumAmount(proto.getMinimumAmount())
                        .benefitLimit(proto.getBenefitLimit())
                        .channel(ChannelType.valueOf(proto.getChannel().name()))
                        .build();
                benefit.getCashbackBenefits().add(newCashbackBenefit);
                hasChanges.set(true);
            } else {
                if (!Objects.equals(dbCashback.getRate(), proto.getRate())) {
                    dbCashback.setRate(proto.getRate()); hasChanges.set(true);
                }
                if (!Objects.equals(dbCashback.getAmount(), proto.getAmount())) {
                    dbCashback.setAmount(proto.getAmount()); hasChanges.set(true);
                }
                if (!Objects.equals(dbCashback.getMinimumAmount(), proto.getMinimumAmount())) {
                    dbCashback.setMinimumAmount(proto.getMinimumAmount()); hasChanges.set(true);
                }
                if (!Objects.equals(dbCashback.getBenefitLimit(), proto.getBenefitLimit())) {
                    dbCashback.setBenefitLimit(proto.getBenefitLimit()); hasChanges.set(true);
                }
                if (dbCashback.getChannel() != ChannelType.valueOf(proto.getChannel().name())) {
                    dbCashback.setChannel(ChannelType.valueOf(proto.getChannel().name())); hasChanges.set(true);
                }
            }
            existingMap.remove(extId);
        }

        existingMap.values().forEach(dbDiscount -> {
            benefit.getDiscountBenefits().remove(dbDiscount);
            hasChanges.set(true);
        });

        return hasChanges.get();
    }

    private boolean updateBenefitDetails(Benefit benefit, CardData.Benefit protoBenefit) {
        boolean changed = false;

        // 기존 applicableCategory, applicableTargets 업데이트
        if (!Objects.equals(benefit.getApplicableCategory(), protoBenefit.getApplicableCategoryList())) {
            benefit.getApplicableCategory().clear();
            benefit.getApplicableCategory().addAll(protoBenefit.getApplicableCategoryList());
            changed = true;
        }
        if (!Objects.equals(benefit.getApplicableTargets(), protoBenefit.getApplicableTargetsList())) {
            benefit.getApplicableTargets().clear();
            benefit.getApplicableTargets().addAll(protoBenefit.getApplicableTargetsList());
            changed = true;
        }

        // 각 세부 혜택 업데이트 호출
        changed = updateDiscountBenefits(benefit, protoBenefit.getDiscountsList()) || changed;
        changed = updatePointBenefits(benefit, protoBenefit.getPointsList()) || changed;
        changed = updateCashbackBenefits(benefit, protoBenefit.getCashbacksList()) || changed;

        return changed;
    }


    private Benefit createBenefit(CardData.Benefit protoBenefit) {
        Benefit benefit = Benefit.builder()
                .externalId(protoBenefit.getBenefitId())
                .applicableCategory(new ArrayList<>(protoBenefit.getApplicableCategoryList()))
                .applicableTargets(new ArrayList<>(protoBenefit.getApplicableTargetsList()))
                .build();
        return benefitRepository.save(benefit);
    }
}

