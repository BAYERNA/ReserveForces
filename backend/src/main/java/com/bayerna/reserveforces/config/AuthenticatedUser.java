package com.bayerna.reserveforces.config;

import com.bayerna.reserveforces.domain.user.Role;

/** JWT 인증 컨텍스트에 담기는 인증 사용자 정보. 컨트롤러에서 @AuthenticationPrincipal로 주입받는다. */
public record AuthenticatedUser(
        Long userId,
        String loginId,
        Role role,
        Long reservistId,
        Long unitId) {
}
