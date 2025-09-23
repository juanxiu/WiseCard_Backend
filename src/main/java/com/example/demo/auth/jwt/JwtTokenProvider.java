package com.example.demo.auth.jwt;

import com.example.demo.auth.dto.TokenResponse;
import com.example.demo.auth.service.RefreshTokenService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;
    private final SecretKey key;

    public JwtTokenProvider(JwtProperties jwtProperties, RefreshTokenService refreshTokenService) {
        this.jwtProperties = jwtProperties;
        this.refreshTokenService = refreshTokenService;
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret()));
    }

    public String createAccessToken(Long userId) {
        return createToken(userId, jwtProperties.getAccessTokenExpirationTime());
    }

    public String createRefreshToken(Long userId) {
        return createToken(userId, jwtProperties.getRefreshTokenExpirationTime());
    }

    private String createToken(Long userId, long expireTime) {
        Date now = new Date();
        return Jwts.builder()
                .issuer("wisecard")
                .claim("id", userId.toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireTime))
                .signWith(key)
                .compact();
    }

    public TokenResponse reissueToken(String token) {
        if (!refreshTokenService.existsToken(token)) {
            throw new RuntimeException("refresh token을 찾을 수 없습니다.");
        }

        Claims claims = validateToken(token);
        Long userId = Long.parseLong(claims.get("id").toString());

        String accessToken = createAccessToken(userId);
        String refreshToken = createRefreshToken(userId);

        refreshTokenService.updateToken(userId, refreshToken);

        return new TokenResponse(accessToken, refreshToken);
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (IllegalArgumentException | UnsupportedJwtException | MalformedJwtException | SecurityException e) {
            throw new RuntimeException("잘못된 토큰입니다.");
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("만료된 토큰입니다.");
        } catch (Exception e) {
            throw new RuntimeException("토큰 검증 중 알 수 없는 오류가 발생했습니다.");
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = validateToken(token);
        String userId = claims.get("id").toString();

        User user = new User(userId, "", Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        return new UsernamePasswordAuthenticationToken(user, "", user.getAuthorities());
    }
}
