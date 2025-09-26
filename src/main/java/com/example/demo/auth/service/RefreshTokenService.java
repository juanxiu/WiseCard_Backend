package com.example.demo.auth.service;

import com.example.demo.auth.entity.RefreshToken;
import com.example.demo.auth.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public boolean existsToken(String token) {
        return refreshTokenRepository.existsByToken(token);
    }

    @Transactional
    public void updateToken(Long userId, String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByMemberId(userId).orElseThrow();
        refreshToken.updateToken(token);
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void save(Long memberId, String token) {
        RefreshToken refreshToken = RefreshToken.builder()
                .memberId(memberId)
                .token(token)
                .build();
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void saveOrUpdateToken(Long memberId, String token) {
        refreshTokenRepository.findByMemberId(memberId).ifPresentOrElse(
                refreshToken -> {
                    refreshToken.updateToken(token);
                    refreshTokenRepository.save(refreshToken);
                },
                () -> {
                    RefreshToken newToken = RefreshToken.builder()
                            .memberId(memberId)
                            .token(token)
                            .build();
                    refreshTokenRepository.save(newToken);
                }
        );
    }
}
