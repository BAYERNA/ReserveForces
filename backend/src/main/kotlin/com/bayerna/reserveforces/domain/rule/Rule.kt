package com.bayerna.reserveforces.domain.rule

import com.bayerna.reserveforces.common.TimestampedEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDate
import java.util.UUID

/**
 * rules (규정 기반 판정 규칙). docs/db-design.md 5.8 참조.
 *
 * 판정 기준을 코드에 하드코딩하지 않고 [conditions] JSONB에 데이터로 저장한다 (FR-RULE-001/002/003).
 * 조건 구조는 `{"field": "arrival_delay_minutes", "operator": "LTE", "value": 60, "unit": "MINUTE"}` 형태이며,
 * 해석은 [com.bayerna.reserveforces.domain.rule.RuleEngine]이 담당한다.
 */
@Entity
@Table(name = "rules")
class Rule(
    @Column(name = "code", nullable = false, unique = true, length = 50)
    var code: String,

    @Column(name = "name", nullable = false, length = 100)
    var name: String,

    @Column(name = "version", nullable = false, length = 30)
    var version: String,

    @Column(name = "description", nullable = false, columnDefinition = "text")
    var description: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 30)
    var ruleType: RuleType,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "conditions", nullable = false, columnDefinition = "jsonb")
    var conditions: String,

    @Column(name = "result_code", nullable = false, length = 30)
    var resultCode: String,

    @Column(name = "priority", nullable = false)
    var priority: Int,

    @Column(name = "effective_from")
    var effectiveFrom: LocalDate? = null,

    @Column(name = "effective_to")
    var effectiveTo: LocalDate? = null,

    @Column(name = "enabled", nullable = false)
    var enabled: Boolean = true,

    id: UUID = UUID.randomUUID(),
) : TimestampedEntity(id)
