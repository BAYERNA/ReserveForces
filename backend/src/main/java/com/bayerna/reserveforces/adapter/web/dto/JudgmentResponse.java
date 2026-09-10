package com.bayerna.reserveforces.adapter.web.dto;

import com.bayerna.reserveforces.domain.judgment.JudgmentOutcome;
import com.bayerna.reserveforces.domain.judgment.JudgmentResult;
import com.bayerna.reserveforces.domain.judgment.JudgmentType;
import java.time.LocalDateTime;

public record JudgmentResponse(
        Long judgmentId,
        Long reservistId,
        String reservistName,
        JudgmentType judgmentType,
        boolean auto,
        JudgmentOutcome result,
        String reason,
        LocalDateTime judgedAt) {

    public static JudgmentResponse from(JudgmentResult judgment) {
        return new JudgmentResponse(
                judgment.getJudgmentId(),
                judgment.getReservist().getReservistId(),
                judgment.getReservist().getDisplayName(),
                judgment.getJudgmentType(),
                judgment.isAuto(),
                judgment.getResult(),
                judgment.getReason(),
                judgment.getJudgedAt());
    }
}
