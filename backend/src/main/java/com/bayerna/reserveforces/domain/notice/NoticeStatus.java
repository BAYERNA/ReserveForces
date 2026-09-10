package com.bayerna.reserveforces.domain.notice;

import java.util.Arrays;

/** 소집통지 상태. DB 컬럼 값은 docs/db-design.md 명세(발송완료 등)를 그대로 사용한다. */
public enum NoticeStatus {
    SENT("발송완료"),
    CONFIRMED("확인완료");

    private final String code;

    NoticeStatus(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static NoticeStatus fromCode(String code) {
        return Arrays.stream(values())
                .filter(v -> v.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 소집통지 상태: " + code));
    }
}
