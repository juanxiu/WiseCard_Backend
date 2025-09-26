package com.example.demo.auth.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
public class KakaoUserInfo {

    private String id;
    private Properties properties;
    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Properties {
        private String nickname;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class KakaoAccount {
        private String email;
    }

    public String getNickName() {
        return this.properties.nickname;
    }

    public String getEmail() {
        return this.kakaoAccount.email;
    }
}
