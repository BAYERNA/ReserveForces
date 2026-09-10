package com.bayerna.reserveforces.adapter.web;

import com.bayerna.reserveforces.adapter.web.dto.LoginRequest;
import com.bayerna.reserveforces.adapter.web.dto.LoginResponse;
import com.bayerna.reserveforces.application.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** FR-01: 로그인 및 권한 분리(RBAC) */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return LoginResponse.from(authService.login(request.loginId(), request.password()));
    }
}
