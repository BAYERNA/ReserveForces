package com.bayerna.reserveforces.application

import com.bayerna.reserveforces.common.ActiveStatus
import com.bayerna.reserveforces.common.ApiException
import com.bayerna.reserveforces.config.AuthenticatedUser
import com.bayerna.reserveforces.config.JwtProvider
import com.bayerna.reserveforces.domain.user.UserRole
import com.bayerna.reserveforces.repository.ReservistRepository
import com.bayerna.reserveforces.repository.UserAccountRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

/** FR-AUTH-001~002: 로그인 및 권한 제어. */
@Service
class AuthService(
    private val userAccountRepository: UserAccountRepository,
    private val reservistRepository: ReservistRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider,
) {

    @Transactional
    fun login(loginId: String, rawPassword: String): LoginResult {
        val account = userAccountRepository.findByLoginId(loginId)
            .orElseThrow { ApiException.unauthorized("아이디 또는 비밀번호가 올바르지 않습니다.") }

        if (!passwordEncoder.matches(rawPassword, account.passwordHash)) {
            throw ApiException.unauthorized("아이디 또는 비밀번호가 올바르지 않습니다.")
        }
        if (account.status != ActiveStatus.ACTIVE) {
            throw ApiException.unauthorized("비활성화된 계정입니다.")
        }

        val reservistId = if (account.role == UserRole.RESERVIST) {
            reservistRepository.findByUser_Id(account.id).map { it.id }.orElse(null)
        } else {
            null
        }

        account.lastLoginAt = OffsetDateTime.now()
        userAccountRepository.save(account)

        val principal = AuthenticatedUser(account.id, account.loginId, account.role, reservistId)
        val token = jwtProvider.generateToken(principal)

        return LoginResult(token = token, role = account.role, displayName = account.name, reservistId = reservistId)
    }
}

data class LoginResult(val token: String, val role: UserRole, val displayName: String, val reservistId: UUID?)
