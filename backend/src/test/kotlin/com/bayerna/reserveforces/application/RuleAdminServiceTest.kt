package com.bayerna.reserveforces.application

import com.bayerna.reserveforces.domain.rule.Rule
import com.bayerna.reserveforces.domain.rule.RuleConditionSpec
import com.bayerna.reserveforces.domain.rule.RuleOperator
import com.bayerna.reserveforces.domain.rule.RuleType
import com.bayerna.reserveforces.repository.RuleRepository
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * FR-RULE-006 / NFR-MAINT-001: 코드 재배포 없이 규정 임계값을 바꿀 수 있어야 하며,
 * 값이 바뀔 때마다 버전이 자동 증가해 이전 판정 근거와 구분되어야 한다.
 */
class RuleAdminServiceTest {

    private lateinit var ruleRepository: RuleRepository
    private lateinit var ruleAdminService: RuleAdminService
    private val objectMapper = jacksonObjectMapper()

    private fun <T> any(): T {
        Mockito.any<T>()
        @Suppress("UNCHECKED_CAST")
        return null as T
    }

    @BeforeEach
    fun setUp() {
        ruleRepository = mock(RuleRepository::class.java)
        ruleAdminService = RuleAdminService(ruleRepository, objectMapper, mock(AuditLogService::class.java))
        `when`(ruleRepository.save(any())).thenAnswer { it.arguments[0] }
    }

    private fun rule(value: Double, version: String = "v1"): Rule {
        val condition = RuleConditionSpec(field = "arrival_delay_minutes", operator = RuleOperator.GT, value = value, unit = "MINUTE")
        return Rule(
            code = "LATE_ENTRY_EXCEEDS_GRACE", name = "지연입소 불허", version = version,
            description = "test", ruleType = RuleType.TIME,
            conditions = objectMapper.writeValueAsString(condition),
            resultCode = "DELAY", priority = 20, id = UUID.randomUUID(),
        )
    }

    @Test
    fun `임계값 변경 시 버전이 1 증가하고 새 값이 반영된다`() {
        val target = rule(value = 60.0, version = "v3")
        `when`(ruleRepository.findById(target.id)).thenReturn(Optional.of(target))

        val updated = ruleAdminService.updateRule(target.id, newValue = 30.0, enabled = null, actor = null)

        assertEquals("v4", updated.version)
        val spec = objectMapper.readValue(updated.conditions, RuleConditionSpec::class.java)
        assertEquals(30.0, spec.value)
    }

    @Test
    fun `enabled만 변경할 때는 버전이 증가하지 않는다`() {
        val target = rule(value = 60.0, version = "v1")
        `when`(ruleRepository.findById(target.id)).thenReturn(Optional.of(target))

        val updated = ruleAdminService.updateRule(target.id, newValue = null, enabled = false, actor = null)

        assertEquals("v1", updated.version)
        assertFalse(updated.enabled)
    }

    @Test
    fun `버전 형식이 v숫자가 아니면 타임스탬프 기반 버전으로 대체한다`() {
        val target = rule(value = 60.0, version = "legacy")
        `when`(ruleRepository.findById(target.id)).thenReturn(Optional.of(target))

        val updated = ruleAdminService.updateRule(target.id, newValue = 45.0, enabled = null, actor = null)

        assert(updated.version.startsWith("v")) { "버전은 v로 시작해야 한다: ${updated.version}" }
        assert(updated.version != "legacy")
    }
}
