package kr.hs.dgsw.ticketzone.domain.member.service;

import kr.hs.dgsw.ticketzone.domain.member.data.request.ReissueRequest;
import kr.hs.dgsw.ticketzone.domain.member.data.request.SignInRequest;
import kr.hs.dgsw.ticketzone.domain.member.data.request.SignUpRequest;
import kr.hs.dgsw.ticketzone.domain.member.data.response.SignInResponse;
import kr.hs.dgsw.ticketzone.domain.member.data.response.SignUpResponse;
import kr.hs.dgsw.ticketzone.domain.member.entity.Member;
import kr.hs.dgsw.ticketzone.domain.member.entity.Role;
import kr.hs.dgsw.ticketzone.domain.member.exception.MemberStatusCode;
import kr.hs.dgsw.ticketzone.domain.member.repository.MemberJpaRepository;
import kr.hs.dgsw.ticketzone.domain.member.service.impl.MemberServiceImpl;
import kr.hs.dgsw.ticketzone.global.exception.CommonException;
import kr.hs.dgsw.ticketzone.global.jwt.JwtProvider;
import kr.hs.dgsw.ticketzone.global.jwt.TokenRedisRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock MemberJpaRepository memberJpaRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtProvider jwtProvider;
    @Mock TokenRedisRepository tokenRedisRepository;

    @InjectMocks MemberServiceImpl memberService;

    private static final String USERNAME = "test1";
    private static final String PASSWORD = "Password1!";
    private static final String ENCODED = "encodedPassword";
    private static final Duration REFRESH_TTL = Duration.ofDays(14);

    private Member member() {
        return Member.builder().username(USERNAME).password(ENCODED).role(Role.ROLE_USER).build();
    }

    private void assertStatus(Throwable e, MemberStatusCode code) {
        assertThat(e).isInstanceOf(CommonException.class);
        assertThat(((CommonException) e).statusCode).isEqualTo(code);
    }

    private void givenTokenIssue() {
        given(jwtProvider.generateAccessToken(USERNAME, Role.ROLE_USER)).willReturn("newAccess");
        given(jwtProvider.generateRefreshToken(USERNAME)).willReturn("newRefresh");
        given(jwtProvider.getRefreshTokenTtl()).willReturn(REFRESH_TTL);
    }

    @Nested
    @DisplayName("signup")
    class SignUp {

        @Test
        @DisplayName("when success, encode password and save ROLE_USER")
        void success() {
            // given
            SignUpRequest request = new SignUpRequest(USERNAME, PASSWORD);
            given(memberJpaRepository.existsByUsername(USERNAME)).willReturn(false);
            given(passwordEncoder.encode(PASSWORD)).willReturn(ENCODED);

            // when
            SignUpResponse response = memberService.signUp(request);

            // then
            ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
            then(memberJpaRepository).should().save(captor.capture());
            Member saved = captor.getValue();
            assertThat(saved.getUsername()).isEqualTo(USERNAME);
            assertThat(saved.getPassword()).isEqualTo(ENCODED);
            assertThat(saved.getRole()).isEqualTo(Role.ROLE_USER);
            assertThat(response.username()).isEqualTo(USERNAME);
        }

        @Test
        @DisplayName("already exisit id")
        void duplicateUsername() {
            // given
            SignUpRequest request = new SignUpRequest(USERNAME, PASSWORD);
            given(memberJpaRepository.existsByUsername(USERNAME)).willReturn(true);

            // when
            Throwable thrown = catchThrowable(() -> memberService.signUp(request));

            // then
            assertStatus(thrown, MemberStatusCode.USERNAME_ALREADY_EXIST);
            then(memberJpaRepository).should(never()).save(any());
        }

        @ParameterizedTest(name = "[{index}] {1}: {0}")
        @CsvSource({
                "Pass1!a,     8자 미만",
                "password1!,  대문자 없음",
                "PASSWORD1!,  소문자 없음",
                "Password!!,  숫자 없음",
                "Password12,  특수문자 없음"
        })
        @DisplayName("week password")
        void weakPassword(String weakPassword, String reason) {
            // given
            SignUpRequest request = new SignUpRequest(USERNAME, weakPassword);
            given(memberJpaRepository.existsByUsername(USERNAME)).willReturn(false);

            // when
            Throwable thrown = catchThrowable(() -> memberService.signUp(request));

            // then
            assertStatus(thrown, MemberStatusCode.PASSWORD_IS_WEAK);
            then(memberJpaRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("sign in")
    class SignIn {

        @Test
        @DisplayName("create pair of tokens and save refresh token to redis")
        void success() {
            // given
            given(memberJpaRepository.findByUsername(USERNAME)).willReturn(Optional.of(member()));
            given(passwordEncoder.matches(PASSWORD, ENCODED)).willReturn(true);
            givenTokenIssue();

            // when
            SignInResponse response = memberService.signIn(new SignInRequest(USERNAME, PASSWORD));

            // then
            assertThat(response.accessToken()).isEqualTo("newAccess");
            assertThat(response.refreshToken()).isEqualTo("newRefresh");
            then(tokenRedisRepository).should().saveRefreshToken(USERNAME, "newRefresh", REFRESH_TTL);
        }

        @Test
        @DisplayName("username not found")
        void unknownUser() {
            // given
            given(memberJpaRepository.findByUsername(USERNAME)).willReturn(Optional.empty());

            // when
            Throwable thrown = catchThrowable(
                    () -> memberService.signIn(new SignInRequest(USERNAME, PASSWORD)));

            // then
            assertStatus(thrown, MemberStatusCode.INVALID_CREDENTIALS);
        }

        @Test
        @DisplayName("not matched password")
        void wrongPassword() {
            // given
            given(memberJpaRepository.findByUsername(USERNAME)).willReturn(Optional.of(member()));
            given(passwordEncoder.matches("wrong", ENCODED)).willReturn(false);

            // when
            Throwable thrown = catchThrowable(
                    () -> memberService.signIn(new SignInRequest(USERNAME, "wrong")));

            // then
            assertStatus(thrown, MemberStatusCode.INVALID_CREDENTIALS);
            then(tokenRedisRepository).should(never()).saveRefreshToken(anyString(), anyString(), any());
        }
    }

    @Nested
    @DisplayName("re create token")
    class Reissue {

        @Test
        @DisplayName("if matched with saved token, create new pair of keys")
        void success() {
            // given
            given(jwtProvider.isValidRefreshToken("oldRefresh")).willReturn(true);
            given(jwtProvider.getUsername("oldRefresh")).willReturn(USERNAME);
            given(tokenRedisRepository.findRefreshToken(USERNAME)).willReturn(Optional.of("oldRefresh"));
            given(memberJpaRepository.findByUsername(USERNAME)).willReturn(Optional.of(member()));
            givenTokenIssue();

            // when
            SignInResponse response = memberService.reissue(new ReissueRequest("oldRefresh"));

            // then
            assertThat(response.accessToken()).isEqualTo("newAccess");
            assertThat(response.refreshToken()).isEqualTo("newRefresh");
            then(tokenRedisRepository).should().saveRefreshToken(USERNAME, "newRefresh", REFRESH_TTL);
        }

        @Test
        @DisplayName("not available refresh token")
        void invalidToken() {
            // given
            given(jwtProvider.isValidRefreshToken("bad")).willReturn(false);

            // when
            Throwable thrown = catchThrowable(
                    () -> memberService.reissue(new ReissueRequest("bad")));

            // then
            assertStatus(thrown, MemberStatusCode.INVALID_REFRESH_TOKEN);
        }

        @Test
        @DisplayName("can not verify refresh token which saved in redis with")
        void notStored() {
            // given
            given(jwtProvider.isValidRefreshToken("oldRefresh")).willReturn(true);
            given(jwtProvider.getUsername("oldRefresh")).willReturn(USERNAME);
            given(tokenRedisRepository.findRefreshToken(USERNAME)).willReturn(Optional.empty());

            // when
            Throwable thrown = catchThrowable(
                    () -> memberService.reissue(new ReissueRequest("oldRefresh")));

            // then
            assertStatus(thrown, MemberStatusCode.INVALID_REFRESH_TOKEN);
        }

        @Test
        @DisplayName("실패: 재사용된 토큰이면 저장된 토큰까지 폐기한다")
        void reusedToken() {
            // given
            given(jwtProvider.isValidRefreshToken("oldRefresh")).willReturn(true);
            given(jwtProvider.getUsername("oldRefresh")).willReturn(USERNAME);
            given(tokenRedisRepository.findRefreshToken(USERNAME)).willReturn(Optional.of("rotatedRefresh"));

            // when
            Throwable thrown = catchThrowable(
                    () -> memberService.reissue(new ReissueRequest("oldRefresh")));

            // then
            assertStatus(thrown, MemberStatusCode.INVALID_REFRESH_TOKEN);
            then(tokenRedisRepository).should().deleteRefreshToken(USERNAME);
        }

        @Test
        @DisplayName("실패: 회원이 존재하지 않음")
        void memberNotFound() {
            // given
            given(jwtProvider.isValidRefreshToken("oldRefresh")).willReturn(true);
            given(jwtProvider.getUsername("oldRefresh")).willReturn(USERNAME);
            given(tokenRedisRepository.findRefreshToken(USERNAME)).willReturn(Optional.of("oldRefresh"));
            given(memberJpaRepository.findByUsername(USERNAME)).willReturn(Optional.empty());

            // when
            Throwable thrown = catchThrowable(
                    () -> memberService.reissue(new ReissueRequest("oldRefresh")));

            // then
            assertStatus(thrown, MemberStatusCode.MEMBER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("sign out")
    class SignOut {

        @Test
        @DisplayName("delete refresh and access token")
        void success() {
            // given
            Duration remaining = Duration.ofMinutes(10);
            given(jwtProvider.getTokenId("access")).willReturn("jti");
            given(jwtProvider.getRemainingTime("access")).willReturn(remaining);

            // when
            memberService.signOut(USERNAME, "access");

            // then
            then(tokenRedisRepository).should().deleteRefreshToken(USERNAME);
            then(tokenRedisRepository).should().addBlacklist("jti", remaining);
        }
    }
}
