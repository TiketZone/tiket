package kr.hs.dgsw.ticketzone.global.jwt;

import kr.hs.dgsw.ticketzone.domain.member.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtProvider {

    private static final String TOKEN_TYPE = "tokenType";
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String REFRESH_TOKEN = "refreshToken";
    private static final String BEARER_PREFIX = "Bearer ";

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtProvider(
            @Value("${spring.jwt.secret}") String secret,
            @Value("${spring.jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${spring.jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    // 엑세스 토큰
    public String generateAccessToken(String username, Role role) {
        Date now = new Date();

        return Jwts.builder()
                .subject(username)
                .claim(TOKEN_TYPE, ACCESS_TOKEN)
                .claim("role", role.name())
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    // 리프레시 토큰
    public String generateRefreshToken(String username) {
        Date now = new Date();

        return Jwts.builder()
                .subject(username)
                .claim(TOKEN_TYPE, REFRESH_TOKEN)
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    public String getTokenId(String token) {
        return getClaims(token).getId();
    }

    // 토큰의 남은 유효시간
    public Duration getRemainingTime(String token) {
        long remaining = getClaims(token).getExpiration().getTime() - System.currentTimeMillis();
        return Duration.ofMillis(Math.max(remaining, 0));
    }

    public Duration getRefreshTokenTtl() {
        return Duration.ofMillis(refreshTokenExpiration);
    }

    // 서명·만료가 유효하고 엑세스 토큰인지 확인
    public boolean isValidAccessToken(String token) {
        return hasTokenType(token, ACCESS_TOKEN);
    }

    // 서명·만료가 유효하고 리프레시 토큰인지 확인
    public boolean isValidRefreshToken(String token) {
        return hasTokenType(token, REFRESH_TOKEN);
    }

    private boolean hasTokenType(String token, String tokenType) {
        try {
            return tokenType.equals(getClaims(token).get(TOKEN_TYPE, String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
