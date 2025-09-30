package com.example.demo.expense.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class ExpenseParsingService {
    
    // 정규식 패턴들
    private static final Pattern PLACE_PATTERN = Pattern.compile("([가-힣\\s]+)에서");
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("([0-9,]+)원");
    
    /**
     * 푸시 알림 텍스트에서 장소와 금액을 파싱
     */
    public ParsedExpenseData parseExpenseText(String text) {
        try {
            String place = extractPlace(text);
            Long amount = extractAmount(text);
            
            return ParsedExpenseData.builder()
                    .place(place)
                    .amount(amount)
                    .originalText(text)
                    .build();
                    
        } catch (Exception e) {
            log.error("푸시 알림 파싱 실패: {}", text, e);
            return null;
        }
    }
    
    private String extractPlace(String text) {
        Matcher matcher = PLACE_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "알 수 없는 장소";
    }
    
    private Long extractAmount(String text) {
        Matcher matcher = AMOUNT_PATTERN.matcher(text);
        if (matcher.find()) {
            String amountStr = matcher.group(1).replace(",", "");
            return Long.parseLong(amountStr);
        }
        return 0L;
    }
    
    @lombok.Builder
    @lombok.Getter
    public static class ParsedExpenseData {
        private String place;
        private Long amount;
        private String originalText;
    }
}


