package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.benefit.dto.CardWithBenefitResponse;
import com.example.demo.card.service.GetCardService;
import com.example.demo.card.service.UserCardService;
import com.example.demo.user.dto.UserCardRegistrationRequest;
import com.example.demo.user.service.UserCardRegistrationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Slf4j
public class CardController {

    private final GetCardService getCardService;
    private final UserCardService userCardService;
    private final UserCardRegistrationService userCardRegistrationService;

   // 카드 리스트 조회 (필터링)
    @GetMapping
    public ResponseEntity<List<CardWithBenefitResponse>> getCards(
            @RequestParam(required = false) String cardBank,
            @RequestParam(required = false) String cardType,
            @RequestParam(required = false) String cardName) {
        
        log.info("카드 조회 요청 - 카드사: {}, 카드타입: {}, 카드명: {}", cardBank, cardType, cardName);
        
        List<CardWithBenefitResponse> cards = getCardService.getCards(cardBank, cardType, cardName);
        return ResponseEntity.ok(cards);
    }

    // 사용자 카드 등록 
    @PostMapping("/register")
    public ResponseEntity<CardWithBenefitResponse> registerCardToUser(@RequestBody UserCardRegistrationRequest request) {
        log.info("사용자 카드 등록 요청 - 사용자: {}, 카드: {}", request.userId(), request.cardId());
        
        try {
            CardWithBenefitResponse registeredCard = userCardRegistrationService.registerCardToUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredCard);
            
        } catch (RuntimeException e) {
            log.warn("사용자 카드 등록 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("사용자 카드 등록 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 사용자 카드 등록 해제 
    @PostMapping("/unregister/{userId}/{cardId}")
    public ResponseEntity<Void> unregisterCardFromUser(@PathVariable Long userId, @PathVariable Long cardId) {
        log.info("사용자 카드 등록 해제 요청 - 사용자: {}, 카드: {}", userId, cardId);
        
        try {
            userCardRegistrationService.unregisterCardFromUser(userId, cardId);
            return ResponseEntity.ok().build();
            
        } catch (RuntimeException e) {
            log.warn("사용자 카드 등록 해제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("사용자 카드 등록 해제 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 사용자별 보유 카드 리스트 조회 
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CardWithBenefitResponse>> getCardsByUserId(
            @PathVariable Long userId,
            @RequestParam(required = false) String cardBank,
            @RequestParam(required = false) String cardType,
            @RequestParam(required = false) String cardName) {
        
        log.info("사용자 카드 조회 요청 - 사용자: {}, 카드사: {}, 카드타입: {}, 카드명: {}", 
                userId, cardBank, cardType, cardName);
        
        List<CardWithBenefitResponse> cards = userCardService.getUserCards(userId, cardBank, cardType, cardName);
        return ResponseEntity.ok(cards);
    }
}

