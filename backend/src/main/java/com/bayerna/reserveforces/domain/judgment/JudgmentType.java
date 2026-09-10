package com.bayerna.reserveforces.domain.judgment;

import java.util.Arrays;

/** 판정 유형. DB 컬럼 값은 docs/db-design.md 명세(지연입소/조기퇴소)를 그대로 사용한다. */
public enum JudgmentType {
    LATE_ENTRY("지연입소"),
    EARLY_DISCHARGE("조기퇴소");

    private final String code;

    JudgmentType(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static JudgmentType fromCode(String code) {
        return Arrays.stream(values())
                .filter(v -> v.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 판정 유형: " + code));
    }
}
