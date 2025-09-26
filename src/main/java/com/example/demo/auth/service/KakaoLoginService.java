package com.example.demo.auth.service;

import com.example.demo.auth.client.KakaoOAuthClient;
import com.example.demo.auth.client.KakaoUserInfo;
import com.example.demo.auth.dto.AccessTokenRequest;
import com.example.demo.auth.dto.RefreshTokenRequest;
import com.example.demo.auth.dto.TokenResponse;
import com.example.demo.auth.entity.Member;
import com.example.demo.auth.jwt.JwtTokenProvider;
import com.example.demo.auth.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.demo.auth.util.AuthUtils.getMemberId;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoLoginService {
    private final MemberRepository memberRepository;
    private final KakaoOAuthClient kakaoOAuthClient;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public TokenResponse signup(AccessTokenRequest request) {
        KakaoUserInfo kakaoUserInfo = kakaoOAuthClient.retrieveUserInfo(request.accessToken());

        if (memberRepository.findBySocialId(kakaoUserInfo.getId()).isPresent()) {
            throw new RuntimeException("이미 가입된 사용자입니다.");
        }

        Member member = Member.builder()
                .socialId(kakaoUserInfo.getId())
                .name(kakaoUserInfo.getNickName())
                .email(kakaoUserInfo.getEmail())
                .build();

        Member newMember = memberRepository.save(member);

        String accessToken = jwtTokenProvider.createAccessToken(newMember.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(newMember.getId());

        refreshTokenService.save(newMember.getId(), refreshToken);

        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public TokenResponse login(AccessTokenRequest request) {
        KakaoUserInfo kakaoUserInfo = kakaoOAuthClient.retrieveUserInfo(request.accessToken());
        Member member = memberRepository.findBySocialId(kakaoUserInfo.getId()).orElseThrow();

        String accessToken = jwtTokenProvider.createAccessToken(member.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());

        refreshTokenService.saveOrUpdateToken(member.getId(), refreshToken);

        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public TokenResponse reissue(RefreshTokenRequest request) {
        return jwtTokenProvider.reissueToken(request.refreshToken());
    }


    @Transactional
    public void withdraw() {
        Long memberId = getMemberId();
        memberRepository.deleteById(memberId);
    }
}
