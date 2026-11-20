package kr.hs.dgsw.ticketzone.global.jwt;

import kr.hs.dgsw.ticketzone.domain.member.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class JwtProviderTest {

    private static final String SECRET = "test-secret-key-test-secret-key-test-secret-key-1234";
    private static final String USERNAME = "tester";

    private final JwtProvider jwtProvider = new JwtProvider(SECRET, 60_000L, 1_209_600_000L);

    @Test
    @DisplayName("can export username and jti by access token")
    void generateAccessToken() {
        // when
        String token = jwtProvider.generateAccessToken(USERNAME, Role.ROLE_USER);

        // then
        assertThat(jwtProvider.isValidAccessToken(token)).isTrue();
        assertThat(jwtProvider.isValidRefreshToken(token)).isFalse();
        assertThat(jwtProvider.getUsername(token)).isEqualTo(USERNAME);
        assertThat(jwtProvider.getTokenId(token)).isNotBlank();
    }

    @Test
    @DisplayName("refresh token can't verified with access token")
    void generateRefreshToken() {
        // when
        String token = jwtProvider.generateRefreshToken(USERNAME);

        // then
        assertThat(jwtProvider.isValidRefreshToken(token)).isTrue();
        assertThat(jwtProvider.isValidAccessToken(token)).isFalse();
    }

    @Test
    @DisplayName("Can't check token verification which signed with another key")
    void invalidSignature() {
        // given
        JwtProvider other = new JwtProvider("another-secret-key-another-secret-key-another-1234", 60_000L, 60_000L);
        String token = other.generateAccessToken(USERNAME, Role.ROLE_USER);

        // when
        boolean valid = jwtProvider.isValidAccessToken(token);

        // then
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("Not verified expired token and invalid token")
    void expiredOrMalformed() {
        // given
        JwtProvider expiredProvider = new JwtProvider(SECRET, -1_000L, -1_000L);
        String expired = expiredProvider.generateAccessToken(USERNAME, Role.ROLE_USER);

        // when, then
        assertThat(jwtProvider.isValidAccessToken(expired)).isFalse();
        assertThat(jwtProvider.isValidAccessToken("wrong token")).isFalse();
        assertThat(jwtProvider.isValidRefreshToken(null)).isFalse();
    }

    @Test
    @DisplayName("available time is under than expired time")
    void remainingTime() {
        // given
        String token = jwtProvider.generateAccessToken(USERNAME, Role.ROLE_USER);

        // when
        Duration remaining = jwtProvider.getRemainingTime(token);

        // then
        assertThat(remaining).isPositive().isLessThanOrEqualTo(Duration.ofMillis(60_000L));
    }

    @Test
    @DisplayName("export token in Bearer header, if type is different, return null")
    void resolveToken() {
        // given
        MockHttpServletRequest bearer = new MockHttpServletRequest();
        bearer.addHeader("Authorization", "Bearer token");
        MockHttpServletRequest basic = new MockHttpServletRequest();
        basic.addHeader("Authorization", "Bearar token");
        MockHttpServletRequest none = new MockHttpServletRequest();

        // when & then
        assertThat(jwtProvider.resolveToken(bearer)).isEqualTo("token");
        assertThat(jwtProvider.resolveToken(basic)).isNull();
        assertThat(jwtProvider.resolveToken(none)).isNull();
    }
}
