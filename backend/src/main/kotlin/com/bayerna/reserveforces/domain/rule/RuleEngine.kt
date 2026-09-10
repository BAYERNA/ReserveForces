package com.bayerna.reserveforces.domain.rule

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

/**
 * 규정 기반 판정을 실행하는 룰엔진 (FR-RULE-001).
 *
 * 판정 기준(지연입소 1시간, 조기퇴소 100km 등)을 코드에 하드코딩하지 않고, [Rule.conditions]에
 * 저장된 조건을 해석하여 실행한다. 이를 통해 규정이 변경되어도 서비스 코드를 수정하지 않고
 * `rules` 테이블의 데이터만 바꾸면 된다 (FR-RULE-006, NFR-MAINT-001).
 */
@Component
class RuleEngine(private val objectMapper: ObjectMapper) {

    /** [rule]의 조건을 [inputs]에 적용해 만족 여부를 판정한다. */
    fun matches(rule: Rule, inputs: Map<String, Double>): Boolean {
        val spec = parseCondition(rule)
        val actual = inputs[spec.field] ?: return false
        return when (spec.operator) {
            RuleOperator.LT -> actual < spec.value
            RuleOperator.LTE -> actual <= spec.value
            RuleOperator.GT -> actual > spec.value
            RuleOperator.GTE -> actual >= spec.value
            RuleOperator.EQ -> actual == spec.value
        }
    }

    /**
     * 우선순위(priority) 오름차순으로 정렬된 [rules] 중 [inputs]를 만족하는 첫 번째 규칙을 반환한다.
     * 일치하는 규칙이 없으면 데이터 누락·미정의 구간으로 간주하여 null을 반환하고,
     * 호출 측에서 EXCEPTION으로 분류한다 (FR-RULE-004).
     */
    fun findFirstMatch(rules: List<Rule>, inputs: Map<String, Double>): Rule? =
        rules.firstOrNull { matches(it, inputs) }

    fun parseCondition(rule: Rule): RuleConditionSpec =
        objectMapper.readValue(rule.conditions, RuleConditionSpec::class.java)

    fun writeCondition(spec: RuleConditionSpec): String = objectMapper.writeValueAsString(spec)
}
