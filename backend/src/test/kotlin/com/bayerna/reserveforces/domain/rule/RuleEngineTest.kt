package com.bayerna.reserveforces.domain.rule

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.LocalDate
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RuleEngineTest {

    private val objectMapper = jacksonObjectMapper()
    private val engine = RuleEngine(objectMapper)

    private fun timeRule(code: String, operator: RuleOperator, value: Double, priority: Int, resultCode: String): Rule {
        val condition = RuleConditionSpec(field = "arrival_delay_minutes", operator = operator, value = value, unit = "MINUTE")
        return Rule(
            code = code,
            name = code,
            version = "v1",
            description = "test",
            ruleType = RuleType.TIME,
            conditions = objectMapper.writeValueAsString(condition),
            resultCode = resultCode,
            priority = priority,
            id = UUID.randomUUID(),
        )
    }

    @Test
    fun `1시간 이내 지연은 NORMAL 규칙에 매칭된다`() {
        val withinGrace = timeRule("LATE_ENTRY_WITHIN_GRACE", RuleOperator.LTE, 60.0, 10, "NORMAL")
        val exceedsGrace = timeRule("LATE_ENTRY_EXCEEDS_GRACE", RuleOperator.GT, 60.0, 20, "DELAY")

        val matched = engine.findFirstMatch(listOf(withinGrace, exceedsGrace), mapOf("arrival_delay_minutes" to 45.0))

        assertEquals("LATE_ENTRY_WITHIN_GRACE", matched?.code)
    }

    @Test
    fun `1시간 초과 지연은 DELAY 규칙에 매칭된다`() {
        val withinGrace = timeRule("LATE_ENTRY_WITHIN_GRACE", RuleOperator.LTE, 60.0, 10, "NORMAL")
        val exceedsGrace = timeRule("LATE_ENTRY_EXCEEDS_GRACE", RuleOperator.GT, 60.0, 20, "DELAY")

        val matched = engine.findFirstMatch(listOf(withinGrace, exceedsGrace), mapOf("arrival_delay_minutes" to 61.0))

        assertEquals("LATE_ENTRY_EXCEEDS_GRACE", matched?.code)
    }

    @Test
    fun `입력값 필드가 없으면 매칭되지 않는다`() {
        val rule = timeRule("LATE_ENTRY_WITHIN_GRACE", RuleOperator.LTE, 60.0, 10, "NORMAL")

        val matched = engine.findFirstMatch(listOf(rule), emptyMap())

        assertNull(matched)
    }

    @Test
    fun `거리 100km 이상은 조기퇴소 허용 규칙에 매칭된다`() {
        val eligible = Rule(
            code = "EARLY_DEPARTURE_ELIGIBLE",
            name = "조기퇴소 허용",
            version = "v1",
            description = "test",
            ruleType = RuleType.DISTANCE,
            conditions = objectMapper.writeValueAsString(
                RuleConditionSpec(field = "distance_km", operator = RuleOperator.GTE, value = 100.0, unit = "KM"),
            ),
            resultCode = "EARLY_DEPARTURE_ELIGIBLE",
            priority = 10,
        )

        val matched = engine.findFirstMatch(listOf(eligible), mapOf("distance_km" to 148.2))

        assertEquals("EARLY_DEPARTURE_ELIGIBLE", matched?.code)
    }

    @Test
    fun `조건 파싱은 effectiveFrom 등 다른 필드와 무관하게 동작한다`() {
        val rule = timeRule("R", RuleOperator.EQ, 0.0, 1, "NORMAL").apply {
            effectiveFrom = LocalDate.of(2026, 1, 1)
        }

        val spec = engine.parseCondition(rule)

        assertEquals("arrival_delay_minutes", spec.field)
    }
}
