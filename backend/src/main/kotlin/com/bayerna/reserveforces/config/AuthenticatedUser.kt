package com.bayerna.reserveforces.config

import com.bayerna.reserveforces.domain.user.UserRole
import java.util.UUID

/** JWT 인증 컨텍스트에 담기는 인증 사용자 정보. 컨트롤러에서 @AuthenticationPrincipal로 주입받는다. */
data class AuthenticatedUser(
    val userId: UUID,
    val loginId: String,
    val role: UserRole,
    val reservistId: UUID?,
)
