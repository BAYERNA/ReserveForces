package com.bayerna.reserveforces.domain.judgment;

import java.util.Arrays;

/** 판정 결과. DB 컬럼 값은 docs/db-design.md 명세(허용/불허)를 그대로 사용한다. */
public enum JudgmentOutcome {
    ALLOWED("허용"),
    DENIED("불허");

    private final String code;

    JudgmentOutcome(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static JudgmentOutcome fromCode(String code) {
        return Arrays.stream(values())
                .filter(v -> v.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 판정 결과: " + code));
    }
}
