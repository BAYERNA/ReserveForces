package com.bayerna.reserveforces.application

import com.bayerna.reserveforces.common.ApiException
import com.bayerna.reserveforces.domain.rule.Rule
import com.bayerna.reserveforces.domain.rule.RuleConditionSpec
import com.bayerna.reserveforces.domain.user.UserAccount
import com.bayerna.reserveforces.repository.RuleRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * FR-RULE-006 / NFR-MAINT-001: 규정 변경 시 서비스 코드를 수정하지 않고
 * rules 테이블의 데이터(기준값·활성 여부)만 바꿀 수 있도록 하는 관리자 기능.
 * 기준값이 바뀌면 규칙 버전을 자동으로 올려, 이전 판정과의 근거를 구분할 수 있게 한다.
 */
@Service
class RuleAdminService(
    private val ruleRepository: RuleRepository,
    private val objectMapper: ObjectMapper,
    private val auditLogService: AuditLogService,
) {

    @Transactional(readOnly = true)
    fun listAll(): List<Rule> =
        ruleRepository.findAll().sortedWith(compareBy({ it.ruleType.name }, { it.priority }))

    @Transactional
    fun updateRule(ruleId: UUID, newValue: Double?, enabled: Boolean?, actor: UserAccount?): Rule {
        if (newValue != null && newValue < 0) {
            throw ApiException.badRequest("규칙 기준값은 0 이상이어야 합니다.")
        }

        val rule = ruleRepository.findById(ruleId)
            .orElseThrow { ApiException.notFound("규칙을 찾을 수 없습니다: $ruleId") }

        val before = mapOf("conditions" to rule.conditions, "enabled" to rule.enabled, "version" to rule.version)

        if (newValue != null) {
            val spec = objectMapper.readValue(rule.conditions, RuleConditionSpec::class.java)
            rule.conditions = objectMapper.writeValueAsString(spec.copy(value = newValue))
            rule.version = bumpVersion(rule.version)
        }
        if (enabled != null) {
            rule.enabled = enabled
        }

        val saved = ruleRepository.save(rule)

        auditLogService.record(
            actor = actor,
            action = "RULE_UPDATED",
            targetType = "rule",
            targetId = ruleId,
            before = before,
            after = mapOf("conditions" to saved.conditions, "enabled" to saved.enabled, "version" to saved.version),
        )

        return saved
    }

    private fun bumpVersion(current: String): String {
        val match = Regex("""^v(\d+)$""").find(current)
        return if (match != null) "v${match.groupValues[1].toInt() + 1}" else "v${System.currentTimeMillis()}"
    }
}
