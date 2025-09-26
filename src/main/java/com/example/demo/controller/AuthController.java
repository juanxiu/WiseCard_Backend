package com.example.demo.controller;

import com.example.demo.auth.dto.AccessTokenRequest;
import com.example.demo.auth.dto.RefreshTokenRequest;
import com.example.demo.auth.dto.TokenResponse;
import com.example.demo.auth.service.KakaoLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final KakaoLoginService kakaoLoginService;

    @PostMapping("/signup")
    public ResponseEntity<TokenResponse> signup(@RequestBody AccessTokenRequest request) {
        try {
            TokenResponse token = kakaoLoginService.signup(request);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody AccessTokenRequest request) {
        try {
            TokenResponse token = kakaoLoginService.login(request);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(@RequestBody RefreshTokenRequest request) {
        TokenResponse token = kakaoLoginService.reissue(request);
        return ResponseEntity.ok(token);
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdraw() {
        kakaoLoginService.withdraw();
        return ResponseEntity.ok().build();
    }
}
