package com.bayerna.reserveforces.domain.reservist;

import java.util.Arrays;

/**
 * 소집대상자 현재 상태. DB 컬럼 값은 docs/db-design.md 명세를 그대로 사용한다
 * (소집전/입영/지연/조기퇴소/완료).
 */
public enum ReservistStatus {
    BEFORE_CALLUP("소집전"),
    ENTERED("입영"),
    LATE("지연"),
    EARLY_DISCHARGE("조기퇴소"),
    COMPLETED("완료");

    private final String code;

    ReservistStatus(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static ReservistStatus fromCode(String code) {
        return Arrays.stream(values())
                .filter(v -> v.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 소집대상자 상태: " + code));
    }
}
