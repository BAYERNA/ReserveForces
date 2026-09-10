package com.bayerna.reserveforces.domain.evaluation

import com.bayerna.reserveforces.common.BaseEntity
import com.bayerna.reserveforces.domain.rule.Rule
import com.bayerna.reserveforces.domain.target.MobilizationTarget
import com.bayerna.reserveforces.domain.user.UserAccount
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime
import java.util.UUID

/**
 * rule_evaluations (판정 실행 결과 및 근거). docs/db-design.md 5.9 참조.
 * FR-RULE-005(판정 근거), FR-RULE-006(규칙 버전)을 위해 실행 당시 입력값 스냅샷과 규칙 버전을 함께 저장한다.
 */
@Entity
@Table(name = "rule_evaluations")
class RuleEvaluation(
    @ManyToOne
    @JoinColumn(name = "target_id", nullable = false)
    var target: MobilizationTarget,

    @ManyToOne
    @JoinColumn(name = "rule_id", nullable = false)
    var rule: Rule,

    @Column(name = "rule_version", nullable = false, length = 30)
    var ruleVersion: String,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input_data", nullable = false, columnDefinition = "jsonb")
    var inputData: String,

    @Column(name = "result_code", nullable = false, length = 30)
    var resultCode: String,

    @Column(name = "result_message", length = 500)
    var resultMessage: String? = null,

    @ManyToOne
    @JoinColumn(name = "evaluated_by")
    var evaluatedBy: UserAccount? = null,

    id: UUID = UUID.randomUUID(),
) : BaseEntity(id) {

    @CreationTimestamp
    @Column(name = "evaluated_at", nullable = false, updatable = false)
    var evaluatedAt: OffsetDateTime? = null
        protected set
}
