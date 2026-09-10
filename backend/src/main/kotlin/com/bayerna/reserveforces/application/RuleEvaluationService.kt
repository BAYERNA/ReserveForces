package com.bayerna.reserveforces.application

import com.bayerna.reserveforces.domain.evaluation.RuleEvaluation
import com.bayerna.reserveforces.domain.rule.Rule
import com.bayerna.reserveforces.domain.rule.RuleEngine
import com.bayerna.reserveforces.domain.rule.RuleType
import com.bayerna.reserveforces.domain.target.MobilizationTarget
import com.bayerna.reserveforces.domain.user.UserAccount
import com.bayerna.reserveforces.repository.RuleEvaluationRepository
import com.bayerna.reserveforces.repository.RuleRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * FR-RULE-001~006: 활성화된 규칙을 실행하고, 판정 근거(입력값 스냅샷·규칙 버전)를 남긴다.
 * 일치하는 규칙이 없으면 null을 반환하며, 호출 측(AttendanceService)이 EXCEPTION으로 분류한다.
 */
@Service
class RuleEvaluationService(
    private val ruleRepository: RuleRepository,
    private val ruleEvaluationRepository: RuleEvaluationRepository,
    private val ruleEngine: RuleEngine,
    private val objectMapper: ObjectMapper,
) {

    @Transactional
    fun evaluate(
        target: MobilizationTarget,
        ruleType: RuleType,
        inputs: Map<String, Double>,
        evaluatedBy: UserAccount?,
    ): RuleEvaluation? {
        val rules = ruleRepository.findByRuleTypeAndEnabledTrueOrderByPriorityAsc(ruleType)
        val matched = ruleEngine.findFirstMatch(rules, inputs) ?: return null
        return recordEvaluation(target, matched, inputs, evaluatedBy)
    }

    private fun recordEvaluation(
        target: MobilizationTarget,
        rule: Rule,
        inputs: Map<String, Double>,
        evaluatedBy: UserAccount?,
    ): RuleEvaluation {
        val evaluation = RuleEvaluation(
            target = target,
            rule = rule,
            ruleVersion = rule.version,
            inputData = objectMapper.writeValueAsString(inputs),
            resultCode = rule.resultCode,
            resultMessage = rule.name,
            evaluatedBy = evaluatedBy,
        )
        return ruleEvaluationRepository.save(evaluation)
    }

    fun listForTarget(targetId: UUID): List<RuleEvaluation> =
        ruleEvaluationRepository.findByTarget_IdOrderByEvaluatedAtDesc(targetId)

    fun listAll(): List<RuleEvaluation> = ruleEvaluationRepository.findAllByOrderByEvaluatedAtDesc()

    fun clearForTarget(targetId: UUID) = ruleEvaluationRepository.deleteByTarget_Id(targetId)
}
