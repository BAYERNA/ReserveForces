package com.bayerna.reserveforces.domain.scenario

import com.bayerna.reserveforces.common.BaseEntity
import com.bayerna.reserveforces.domain.mobilization.Mobilization
import com.bayerna.reserveforces.domain.user.UserAccount
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.OffsetDateTime
import java.util.UUID

/** scenario_runs (시나리오 실행 기록). docs/db-design.md 5.11 참조. */
@Entity
@Table(name = "scenario_runs")
class ScenarioRun(
    @ManyToOne
    @JoinColumn(name = "scenario_id", nullable = false)
    var scenario: Scenario,

    @ManyToOne
    @JoinColumn(name = "mobilization_id")
    var mobilization: Mobilization? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "run_status", nullable = false, length = 20)
    var runStatus: ScenarioRunStatus,

    @Column(name = "completed_at")
    var completedAt: OffsetDateTime? = null,

    @ManyToOne
    @JoinColumn(name = "executed_by")
    var executedBy: UserAccount? = null,

    id: UUID = UUID.randomUUID(),
) : BaseEntity(id) {

    @CreationTimestamp
    @Column(name = "started_at", nullable = false, updatable = false)
    var startedAt: OffsetDateTime? = null
        protected set
}
