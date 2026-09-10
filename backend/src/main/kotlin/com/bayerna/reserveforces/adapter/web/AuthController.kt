package com.bayerna.reserveforces.adapter.web

import com.bayerna.reserveforces.adapter.web.dto.LoginRequest
import com.bayerna.reserveforces.adapter.web.dto.LoginResponse
import com.bayerna.reserveforces.application.AuthService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/** FR-AUTH-001~002: 로그인 및 권한 제어. */
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): LoginResponse =
        LoginResponse.from(authService.login(request.loginId, request.password))
}
