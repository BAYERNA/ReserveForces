package com.bayerna.reserveforces.adapter.web.dto

import com.bayerna.reserveforces.application.LoginResult
import com.bayerna.reserveforces.domain.user.UserRole
import jakarta.validation.constraints.NotBlank
import java.util.UUID

data class LoginRequest(
    @field:NotBlank(message = "로그인 아이디를 입력해 주세요.") val loginId: String,
    @field:NotBlank(message = "비밀번호를 입력해 주세요.") val password: String,
)

data class LoginResponse(
    val token: String,
    val role: UserRole,
    val displayName: String,
    val reservistId: UUID?,
) {
    companion object {
        fun from(result: LoginResult) =
            LoginResponse(result.token, result.role, result.displayName, result.reservistId)
    }
}
