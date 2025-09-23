package com.example.demo.auth.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class KakaoOAuthClient {

    private final WebClient webClient;

    public KakaoOAuthClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://kapi.kakao.com").build();
    }

    public KakaoUserInfo retrieveUserInfo(String accessToken) {
        try {
            return webClient.get()
                    .uri("/v2/user/me")
                    .headers(headers -> headers.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(KakaoUserInfo.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("카카오 API 호출 실패: " + e.getResponseBodyAsString(), e);
        }
    }
}