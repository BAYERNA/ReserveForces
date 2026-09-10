package com.bayerna.reserveforces.repository

import com.bayerna.reserveforces.domain.attendance.Attendance
import com.bayerna.reserveforces.domain.attendance.AttendanceStatus
import com.bayerna.reserveforces.domain.audit.AuditLog
import com.bayerna.reserveforces.domain.evaluation.RuleEvaluation
import com.bayerna.reserveforces.domain.location.Location
import com.bayerna.reserveforces.domain.mobilization.Mobilization
import com.bayerna.reserveforces.domain.reservist.Reservist
import com.bayerna.reserveforces.domain.rule.Rule
import com.bayerna.reserveforces.domain.rule.RuleType
import com.bayerna.reserveforces.domain.scenario.Scenario
import com.bayerna.reserveforces.domain.scenario.ScenarioRun
import com.bayerna.reserveforces.domain.target.MobilizationTarget
import com.bayerna.reserveforces.domain.target.TargetStatus
import com.bayerna.reserveforces.domain.unit.MilitaryUnit
import com.bayerna.reserveforces.domain.user.UserAccount
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface UserAccountRepository : JpaRepository<UserAccount, UUID> {
    fun findByLoginId(loginId: String): Optional<UserAccount>
}

interface MilitaryUnitRepository : JpaRepository<MilitaryUnit, UUID> {
    fun findByCode(code: String): Optional<MilitaryUnit>
}

interface LocationRepository : JpaRepository<Location, UUID>

interface ReservistRepository : JpaRepository<Reservist, UUID> {
    fun findByUser_Id(userId: UUID): Optional<Reservist>
    fun findByDemoIdentifier(demoIdentifier: String): Optional<Reservist>
}

interface MobilizationRepository : JpaRepository<Mobilization, UUID>

interface MobilizationTargetRepository : JpaRepository<MobilizationTarget, UUID> {
    fun findByMobilization_Id(mobilizationId: UUID): List<MobilizationTarget>
    fun findByMobilization_IdAndTargetStatus(mobilizationId: UUID, targetStatus: TargetStatus): List<MobilizationTarget>
    fun findByReservist_Id(reservistId: UUID): List<MobilizationTarget>
    fun findTopByReservist_IdOrderByCreatedAtDesc(reservistId: UUID): Optional<MobilizationTarget>
    fun existsByMobilization_IdAndReservist_Id(mobilizationId: UUID, reservistId: UUID): Boolean
}

interface AttendanceRepository : JpaRepository<Attendance, UUID> {
    fun findByTarget_Id(targetId: UUID): Optional<Attendance>
    fun countByAttendanceStatus(status: AttendanceStatus): Long
}

interface RuleRepository : JpaRepository<Rule, UUID> {
    fun findByCode(code: String): Optional<Rule>
    fun findByRuleTypeAndEnabledTrueOrderByPriorityAsc(ruleType: RuleType): List<Rule>
}

interface RuleEvaluationRepository : JpaRepository<RuleEvaluation, UUID> {
    fun findByTarget_IdOrderByEvaluatedAtDesc(targetId: UUID): List<RuleEvaluation>
    fun findAllByOrderByEvaluatedAtDesc(): List<RuleEvaluation>
    fun deleteByTarget_Id(targetId: UUID)
}

interface ScenarioRepository : JpaRepository<Scenario, UUID> {
    fun findByCode(code: String): Optional<Scenario>
}

interface ScenarioRunRepository : JpaRepository<ScenarioRun, UUID>

interface AuditLogRepository : JpaRepository<AuditLog, UUID> {
    fun findAllByOrderByCreatedAtDesc(): List<AuditLog>
}
