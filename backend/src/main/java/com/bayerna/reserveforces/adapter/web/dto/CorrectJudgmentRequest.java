package com.bayerna.reserveforces.adapter.web.dto;

import com.bayerna.reserveforces.domain.judgment.JudgmentOutcome;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** FR-06: 판정 결과 수동 보정 요청. */
public record CorrectJudgmentRequest(
        @NotNull(message = "보정할 판정 결과를 선택해 주세요.") JudgmentOutcome result,
        @NotBlank(message = "보정 사유를 입력해 주세요.") String reason) {
}
