package kr.hs.dgsw.ticketzone.domain.member.service.impl;

import kr.hs.dgsw.ticketzone.domain.member.data.request.ReissueRequest;
import kr.hs.dgsw.ticketzone.domain.member.data.request.SignInRequest;
import kr.hs.dgsw.ticketzone.domain.member.data.request.SignUpRequest;
import kr.hs.dgsw.ticketzone.domain.member.data.response.SignInResponse;
import kr.hs.dgsw.ticketzone.domain.member.data.response.SignUpResponse;
import kr.hs.dgsw.ticketzone.domain.member.entity.Member;
import kr.hs.dgsw.ticketzone.domain.member.entity.Role;
import kr.hs.dgsw.ticketzone.domain.member.exception.MemberStatusCode;
import kr.hs.dgsw.ticketzone.domain.member.repository.MemberJpaRepository;
import kr.hs.dgsw.ticketzone.domain.member.service.MemberService;
import kr.hs.dgsw.ticketzone.global.exception.CommonException;
import kr.hs.dgsw.ticketzone.global.jwt.JwtProvider;
import kr.hs.dgsw.ticketzone.global.jwt.TokenRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberJpaRepository memberJpaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final TokenRedisRepository tokenRedisRepository;

    // 일반 사용자 회원가입(관리자 회원가입은 존재하지 않음, 따로 DB에 직접 넣는 구조로 설계)
    @Override
    public SignUpResponse signUp(SignUpRequest signUpRequest) {
        if (memberJpaRepository.existsByUsername(signUpRequest.username())) {
            throw new CommonException(MemberStatusCode.USERNAME_ALREADY_EXIST);
        }

        if (!isValidPassword(signUpRequest.password())) {
            throw new CommonException(MemberStatusCode.PASSWORD_IS_WEAK);
        }

        Member member = Member.builder()
                .username(signUpRequest.username())
                .password(passwordEncoder.encode(signUpRequest.password()))
                .role(Role.ROLE_USER)
                .build();

        memberJpaRepository.save(member);

        return SignUpResponse.of(member);
    }

    // 로그인 + 토큰 발급
    @Override
    @Transactional(readOnly = true)
    public SignInResponse signIn(SignInRequest signInRequest) {
        Member member = memberJpaRepository.findByUsername(signInRequest.username())
                .orElseThrow(() -> new CommonException(MemberStatusCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(signInRequest.password(), member.getPassword())) {
            throw new CommonException(MemberStatusCode.INVALID_CREDENTIALS);
        }

        return issueTokens(member);
    }

    // 토큰 재발급: Redis에 저장된 리프레시 토큰과 일치할 때만 새 토큰 쌍 발급(rotation)
    @Override
    @Transactional(readOnly = true)
    public SignInResponse reissue(ReissueRequest reissueRequest) {
        String refreshToken = reissueRequest.refreshToken();

        if (!jwtProvider.isValidRefreshToken(refreshToken)) {
            throw new CommonException(MemberStatusCode.INVALID_REFRESH_TOKEN);
        }

        String username = jwtProvider.getUsername(refreshToken);
        String savedRefreshToken = tokenRedisRepository.findRefreshToken(username)
                .orElseThrow(() -> new CommonException(MemberStatusCode.INVALID_REFRESH_TOKEN));

        // 이미 교체된(재사용된) 토큰이면 탈취로 보고 저장된 토큰까지 폐기
        if (!savedRefreshToken.equals(refreshToken)) {
            tokenRedisRepository.deleteRefreshToken(username);
            throw new CommonException(MemberStatusCode.INVALID_REFRESH_TOKEN);
        }

        Member member = memberJpaRepository.findByUsername(username)
                .orElseThrow(() -> new CommonException(MemberStatusCode.MEMBER_NOT_FOUND));

        return issueTokens(member);
    }

    // 로그아웃: 리프레시 토큰 삭제 + 엑세스 토큰은 남은 시간 동안 블랙리스트 처리
    @Override
    public void signOut(String username, String accessToken) {
        tokenRedisRepository.deleteRefreshToken(username);
        tokenRedisRepository.addBlacklist(
                jwtProvider.getTokenId(accessToken),
                jwtProvider.getRemainingTime(accessToken)
        );
    }

    // 토큰 쌍 발급 + 리프레시 토큰 Redis 저장
    private SignInResponse issueTokens(Member member) {
        String accessToken = jwtProvider.generateAccessToken(member.getUsername(), member.getRole());
        String refreshToken = jwtProvider.generateRefreshToken(member.getUsername());

        tokenRedisRepository.saveRefreshToken(member.getUsername(), refreshToken, jwtProvider.getRefreshTokenTtl());

        return SignInResponse.of(accessToken, refreshToken);
    }

    // 비밀번호 검증(8자 이상, 대문자·소문자·숫자·특수문자 포함)
    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) { return false; }

        boolean hasUpperCase = password.matches(".*[A-Z].*");
        boolean hasLowerCase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");

        return hasUpperCase && hasLowerCase && hasDigit && hasSpecial;
    }
}
