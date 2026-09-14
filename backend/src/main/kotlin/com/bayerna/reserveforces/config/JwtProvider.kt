package com.bayerna.reserveforces.config

import com.bayerna.reserveforces.domain.user.UserRole
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Component
class JwtProvider(private val properties: JwtProperties) {

    private val log = LoggerFactory.getLogger(JwtProvider::class.java)

    /**
     * application.yml의 app.jwt.secret 기본값은 저장소에 공개된 고정 문자열이므로,
     * 배포 시 JWT_SECRET 환경변수로 재정의하지 않으면 코드를 볼 수 있는 누구나 서명 키를 알고
     * 임의 권한(ADMIN 등)의 위조 토큰을 만들 수 있다. 기본값이 그대로 사용될 경우
     * 매 기동 시 무작위 키를 생성해 이 위조를 원천 차단한다(오프라인 데모 특성상
     * 재기동 시 기존 세션 재로그인만 필요하며 별도 설정 없이 안전하게 동작한다).
     */
    private val signingKey: SecretKey = run {
        val secret = if (properties.secret == DEFAULT_SECRET) {
            log.warn(
                "JWT_SECRET 환경변수가 설정되지 않아 저장소에 공개된 기본 시크릿이 감지되었습니다. " +
                    "보안을 위해 매 기동 시 무작위 서명 키를 생성합니다. 운영 배포 시 JWT_SECRET을 반드시 지정하세요.",
            )
            Keys.hmacShaKeyFor(Jwts.SIG.HS512.key().build().encoded)
        } else {
            Keys.hmacShaKeyFor(properties.secret.toByteArray(StandardCharsets.UTF_8))
        }
        secret
    }

    fun generateToken(user: AuthenticatedUser): String {
        val now = Instant.now()
        val builder = Jwts.builder()
            .subject(user.loginId)
            .claim(CLAIM_USER_ID, user.userId.toString())
            .claim(CLAIM_ROLE, user.role.name)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(properties.expirationMinutes, ChronoUnit.MINUTES)))
            .signWith(signingKey)

        user.reservistId?.let { builder.claim(CLAIM_RESERVIST_ID, it.toString()) }

        return builder.compact()
    }

    fun parseToken(token: String): AuthenticatedUser {
        val claims = Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload

        return AuthenticatedUser(
            userId = UUID.fromString(claims.get(CLAIM_USER_ID, String::class.java)),
            loginId = claims.subject,
            role = UserRole.valueOf(claims.get(CLAIM_ROLE, String::class.java)),
            reservistId = claims.get(CLAIM_RESERVIST_ID, String::class.java)?.let(UUID::fromString),
        )
    }

    companion object {
        private const val CLAIM_USER_ID = "userId"
        private const val CLAIM_ROLE = "role"
        private const val CLAIM_RESERVIST_ID = "reservistId"

        /** application.yml의 app.jwt.secret 기본값과 반드시 동일해야 한다. */
        private const val DEFAULT_SECRET = "EDaGYbsZyG7oiza5x0FR73CvyQ5PsRvQKbTq8Q++4e9SzYBP81024/WHyhY7HwaM"
    }
}
