package com.example.demo.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.benefit.dto.CardWithBenefitResponse;
import com.example.demo.benefit.util.BenefitConverter;
import com.example.demo.card.entity.Card;
import com.example.demo.card.repository.CardRepository;
import com.example.demo.user.dto.UserCardRegistrationRequest;
import com.example.demo.user.entity.UserCard;
import com.example.demo.user.repository.UserCardRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCardRegistrationService {
    private final UserCardRepository userCardRepository;
    private final CardRepository cardRepository;
    private final BenefitConverter benefitConverter;

    // 사용자 카드 등록
    @Transactional
    public CardWithBenefitResponse registerCardToUser(UserCardRegistrationRequest request) {
        log.info("사용자 카드 등록 요청 - 사용자: {}, 카드: {}", request.userId(), request.cardId());

        // 카드 존재 확인
        Card card = cardRepository.findById(request.cardId())
                .orElseThrow(() -> new RuntimeException("카드를 찾을 수 없습니다: " + request.cardId()));

        // 이미 등록된 카드인지 확인
        if (userCardRepository.existsByUserIdAndCard_IdAndIsActiveTrue(request.userId(), request.cardId())) {
            log.warn("이미 등록된 카드입니다 - 사용자: {}, 카드: {}", request.userId(), request.cardId());
            throw new RuntimeException("이미 등록된 카드입니다");
        }

        // UserCard 엔티티 생성
        UserCard userCard = UserCard.builder()
                .userId(request.userId())
                .card(card)
                .isActive(true)
                .build();

        // 저장
        userCardRepository.save(userCard);
        
        log.info("사용자 카드 등록 완료 - 사용자: {}, 카드: {}", request.userId(), request.cardId());

        // 응답 DTO 생성
        return CardWithBenefitResponse.builder()
                .cardId(card.getId())
                .cardName(card.getCardName())
                .cardBank(card.getCardBank())
                .imgUrl(card.getImgUrl())
                .type(card.getType())
                .benefits(benefitConverter.convertMultipleBenefitsToDTO(card.getBenefits()))
                .build();
    }

    // 사용자 카드 등록 해제
    @Transactional
    public void unregisterCardFromUser(Long userId, Long cardId) {
        log.info("사용자 카드 등록 해제 요청 - 사용자: {}, 카드: {}", userId, cardId);

        UserCard userCard = userCardRepository.findByUserIdAndCardIdAndIsActiveTrue(userId, cardId)
                .orElseThrow(() -> new RuntimeException("등록된 카드를 찾을 수 없습니다"));

        userCard.setIsActive(false);
        userCardRepository.save(userCard);
        
        log.info("사용자 카드 등록 해제 완료 - 사용자: {}, 카드: {}", userId, cardId);
    }
}
