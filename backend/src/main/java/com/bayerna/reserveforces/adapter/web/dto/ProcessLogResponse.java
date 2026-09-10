package com.bayerna.reserveforces.adapter.web.dto;

import com.bayerna.reserveforces.domain.judgment.JudgmentType;
import com.bayerna.reserveforces.domain.processlog.ProcessLog;
import java.time.LocalDateTime;

public record ProcessLogResponse(
        Long logId,
        Long judgmentId,
        String reservistName,
        JudgmentType judgmentType,
        String processedBy,
        LocalDateTime processedAt,
        String content) {

    public static ProcessLogResponse from(ProcessLog log) {
        return new ProcessLogResponse(
                log.getLogId(),
                log.getJudgment().getJudgmentId(),
                log.getJudgment().getReservist().getDisplayName(),
                log.getJudgment().getJudgmentType(),
                log.getUser().getLoginId(),
                log.getProcessedAt(),
                log.getContent());
    }
}
