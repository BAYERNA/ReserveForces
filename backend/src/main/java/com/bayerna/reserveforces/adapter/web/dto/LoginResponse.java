package com.bayerna.reserveforces.adapter.web.dto;

import com.bayerna.reserveforces.application.AuthService;
import com.bayerna.reserveforces.domain.user.Role;

public record LoginResponse(String token, Role role, String displayName, Long reservistId, Long unitId) {

    public static LoginResponse from(AuthService.LoginResult result) {
        return new LoginResponse(result.token(), result.role(), result.displayName(), result.reservistId(), result.unitId());
    }
}
