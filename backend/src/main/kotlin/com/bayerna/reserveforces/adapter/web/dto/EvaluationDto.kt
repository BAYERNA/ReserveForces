package com.bayerna.reserveforces.adapter.web.dto

import com.bayerna.reserveforces.domain.evaluation.RuleEvaluation
import java.time.OffsetDateTime
import java.util.UUID

data class RuleEvaluationResponse(
    val evaluationId: UUID,
    val targetId: UUID,
    val reservistName: String,
    val ruleCode: String,
    val ruleName: String,
    val ruleVersion: String,
    val inputData: String,
    val resultCode: String,
    val resultMessage: String?,
    val evaluatedAt: OffsetDateTime?,
    val evaluatedBy: String?,
) {
    companion object {
        fun from(evaluation: RuleEvaluation) = RuleEvaluationResponse(
            evaluationId = evaluation.id,
            targetId = evaluation.target.id,
            reservistName = evaluation.target.reservist.name,
            ruleCode = evaluation.rule.code,
            ruleName = evaluation.rule.name,
            ruleVersion = evaluation.ruleVersion,
            inputData = evaluation.inputData,
            resultCode = evaluation.resultCode,
            resultMessage = evaluation.resultMessage,
            evaluatedAt = evaluation.evaluatedAt,
            evaluatedBy = evaluation.evaluatedBy?.name,
        )
    }
}
