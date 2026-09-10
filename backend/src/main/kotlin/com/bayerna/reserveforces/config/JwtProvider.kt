package com.bayerna.reserveforces.config

import com.bayerna.reserveforces.domain.user.UserRole
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Component
class JwtProvider(private val properties: JwtProperties) {

    private val signingKey: SecretKey = Keys.hmacShaKeyFor(properties.secret.toByteArray(StandardCharsets.UTF_8))

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
    }
}
