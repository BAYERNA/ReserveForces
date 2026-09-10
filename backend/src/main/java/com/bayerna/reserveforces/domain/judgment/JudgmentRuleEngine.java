package com.bayerna.reserveforces.domain.judgment;

import com.bayerna.reserveforces.domain.entry.EntryStatus;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

/**
 * 지연입소·조기퇴소 자동판정 규정을 코드화한 순수 도메인 룰엔진. (FR-04, FR-05)
 *
 * <ul>
 *   <li>지연입소: 지정 입영시각을 초과하여 입영한 경우, 초과 시간이 1시간 이내이면 허용, 초과하면 불허</li>
 *   <li>조기퇴소: 거주지-소집부대 간 거리가 100km 이상이면 허용, 미만이면 불허</li>
 * </ul>
 */
@Component
public class JudgmentRuleEngine {

    public static final Duration LATE_ENTRY_GRACE_PERIOD = Duration.ofHours(1);
    public static final BigDecimal EARLY_DISCHARGE_DISTANCE_THRESHOLD_KM = BigDecimal.valueOf(100);

    /**
     * 실제 입영 일시를 지정 입영시각과 비교하여 입영 상태 및 지연입소 판정을 산출한다.
     *
     * @param scheduledEntryDeadline 지정 입영 마감 일시 (소집부대 entry_deadline_time을 소집예정일에 적용한 값)
     * @param actualEntryDatetime    실제 입영 일시 (미입영 시 null)
     */
    public LateEntryJudgment judgeLateEntry(LocalDateTime scheduledEntryDeadline, LocalDateTime actualEntryDatetime) {
        if (actualEntryDatetime == null) {
            return new LateEntryJudgment(false, null, EntryStatus.NO_SHOW, Duration.ZERO);
        }

        if (!actualEntryDatetime.isAfter(scheduledEntryDeadline)) {
            return new LateEntryJudgment(false, null, EntryStatus.COMPLETED, Duration.ZERO);
        }

        Duration lateBy = Duration.between(scheduledEntryDeadline, actualEntryDatetime);
        JudgmentOutcome outcome = lateBy.compareTo(LATE_ENTRY_GRACE_PERIOD) <= 0
                ? JudgmentOutcome.ALLOWED
                : JudgmentOutcome.DENIED;
        return new LateEntryJudgment(true, outcome, EntryStatus.LATE, lateBy);
    }

    /**
     * 거주지-소집부대 간 거리를 기준으로 조기퇴소 허용 여부를 판정한다.
     */
    public EarlyDischargeJudgment judgeEarlyDischarge(BigDecimal residenceDistanceKm) {
        JudgmentOutcome outcome = residenceDistanceKm.compareTo(EARLY_DISCHARGE_DISTANCE_THRESHOLD_KM) >= 0
                ? JudgmentOutcome.ALLOWED
                : JudgmentOutcome.DENIED;
        return new EarlyDischargeJudgment(outcome);
    }

    /** 지연입소 판정 결과. applicable=false면 판정결과(JUDGMENT_RESULT) 레코드를 생성하지 않는다. */
    public record LateEntryJudgment(
            boolean applicable,
            JudgmentOutcome outcome,
            EntryStatus entryStatus,
            Duration lateBy) {
    }

    /** 조기퇴소 판정 결과. 모든 소집대상자에 대해 항상 산출된다. */
    public record EarlyDischargeJudgment(JudgmentOutcome outcome) {
    }
}
