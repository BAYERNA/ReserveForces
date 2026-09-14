package com.bayerna.reserveforces.domain.target

/** 소집 대상 상태 전이: EXPECTED -> (ARRIVED -> NORMAL/DELAY) | ABSENT | EXCEPTION -> COMPLETED. */
enum class TargetStatus {
    EXPECTED,
    ARRIVED,
    DELAYED,
    ABSENT,
    EXCEPTION,
    COMPLETED,
}
