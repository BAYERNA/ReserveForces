package com.bayerna.reserveforces.application;

import com.bayerna.reserveforces.common.ApiException;
import com.bayerna.reserveforces.config.AuthenticatedUser;
import com.bayerna.reserveforces.config.JwtProvider;
import com.bayerna.reserveforces.domain.user.Role;
import com.bayerna.reserveforces.domain.user.UserAccount;
import com.bayerna.reserveforces.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** FR-01: 로그인 및 권한 분리(RBAC) */
@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AuthService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            JwtProvider jwtProvider) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    public LoginResult login(String loginId, String rawPassword) {
        UserAccount account = userAccountRepository.findByLoginId(loginId)
                .orElseThrow(() -> ApiException.unauthorized("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(rawPassword, account.getPasswordHash())) {
            throw ApiException.unauthorized("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        Long reservistId = account.getReservist() != null ? account.getReservist().getReservistId() : null;
        Long unitId = account.getUnit() != null ? account.getUnit().getUnitId() : null;

        AuthenticatedUser principal = new AuthenticatedUser(
                account.getUserId(), account.getLoginId(), account.getRole(), reservistId, unitId);
        String token = jwtProvider.generateToken(principal);

        String displayName = account.getRole() == Role.RESERVIST && account.getReservist() != null
                ? account.getReservist().getDisplayName()
                : account.getUnit() != null
                        ? account.getUnit().getUnitName() + " 담당자"
                        : "관리자";

        return new LoginResult(token, account.getRole(), displayName, reservistId, unitId);
    }

    public record LoginResult(String token, Role role, String displayName, Long reservistId, Long unitId) {
    }
}
