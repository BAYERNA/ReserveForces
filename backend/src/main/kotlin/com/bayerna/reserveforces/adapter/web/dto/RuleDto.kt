package com.bayerna.reserveforces.adapter.web.dto

import com.bayerna.reserveforces.domain.rule.Rule
import com.bayerna.reserveforces.domain.rule.RuleType
import java.util.UUID

data class RuleResponse(
    val ruleId: UUID,
    val code: String,
    val name: String,
    val version: String,
    val description: String,
    val ruleType: RuleType,
    val conditions: String,
    val resultCode: String,
    val priority: Int,
    val enabled: Boolean,
) {
    companion object {
        fun from(rule: Rule) = RuleResponse(
            ruleId = rule.id,
            code = rule.code,
            name = rule.name,
            version = rule.version,
            description = rule.description,
            ruleType = rule.ruleType,
            conditions = rule.conditions,
            resultCode = rule.resultCode,
            priority = rule.priority,
            enabled = rule.enabled,
        )
    }
}

/** value가 주어지면 conditions.value를 교체하고 규칙 버전을 자동으로 올린다. */
data class UpdateRuleRequest(
    val value: Double? = null,
    val enabled: Boolean? = null,
)
