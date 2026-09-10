package com.bayerna.reserveforces.domain.entry;

import java.util.Arrays;

/** 입영기록 상태. DB 컬럼 값은 docs/db-design.md 명세(대기/완료/지연/미입영)를 그대로 사용한다. */
public enum EntryStatus {
    WAITING("대기"),
    COMPLETED("완료"),
    LATE("지연"),
    NO_SHOW("미입영");

    private final String code;

    EntryStatus(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static EntryStatus fromCode(String code) {
        return Arrays.stream(values())
                .filter(v -> v.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 입영기록 상태: " + code));
    }
}
