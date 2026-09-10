package com.bayerna.reserveforces.config;

import com.bayerna.reserveforces.domain.user.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_RESERVIST_ID = "reservistId";
    private static final String CLAIM_UNIT_ID = "unitId";

    private final SecretKey signingKey;
    private final long expirationMinutes;

    public JwtProvider(JwtProperties properties) {
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = properties.expirationMinutes();
    }

    public String generateToken(AuthenticatedUser user) {
        Instant now = Instant.now();
        JwtBuilder builder = Jwts.builder()
                .subject(user.loginId())
                .claim(CLAIM_USER_ID, user.userId())
                .claim(CLAIM_ROLE, user.role().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)))
                .signWith(signingKey);
        if (user.reservistId() != null) {
            builder.claim(CLAIM_RESERVIST_ID, user.reservistId());
        }
        if (user.unitId() != null) {
            builder.claim(CLAIM_UNIT_ID, user.unitId());
        }
        return builder.compact();
    }

    public AuthenticatedUser parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new AuthenticatedUser(
                toLong(claims.get(CLAIM_USER_ID)),
                claims.getSubject(),
                Role.valueOf(claims.get(CLAIM_ROLE, String.class)),
                toLong(claims.get(CLAIM_RESERVIST_ID)),
                toLong(claims.get(CLAIM_UNIT_ID)));
    }

    private static Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(value.toString());
    }
}
