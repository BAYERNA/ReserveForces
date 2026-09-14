package com.bayerna.reserveforces.domain.user

/** 계정 역할. 시연 관리자(DEMO)는 발표용 시나리오 실행/초기화 전용 권한을 가진다. */
enum class UserRole {
    ADMIN,
    RESERVIST,
    DEMO,
}
