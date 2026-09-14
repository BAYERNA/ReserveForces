package com.bayerna.reserveforces.domain.rule

/**
 * [Rule.conditions] JSONB의 구조. 예:
 * `{"field": "arrival_delay_minutes", "operator": "LTE", "value": 60, "unit": "MINUTE"}`
 *
 * docs/db-design.md 9절의 Rule 데이터 구조 예시를 단순화하여, 한 Rule 행이
 * "하나의 필드에 대한 하나의 비교 조건"을 표현하도록 설계했다. 같은 rule_type에 대해
 * 여러 Rule 행(우선순위 순)을 등록하면 사실상 규정의 여러 구간(예: 1시간 이내/초과)을
 * 코드 변경 없이 표현할 수 있다.
 */
data class RuleConditionSpec(
    val field: String,
    val operator: RuleOperator,
    val value: Double,
    val unit: String? = null,
)

enum class RuleOperator {
    LT,
    LTE,
    GT,
    GTE,
    EQ,
}
