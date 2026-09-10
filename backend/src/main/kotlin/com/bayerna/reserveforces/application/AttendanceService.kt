package com.bayerna.reserveforces.application

import com.bayerna.reserveforces.common.ApiException
import com.bayerna.reserveforces.domain.attendance.Attendance
import com.bayerna.reserveforces.domain.attendance.AttendanceStatus
import com.bayerna.reserveforces.domain.rule.RuleType
import com.bayerna.reserveforces.domain.target.MobilizationTarget
import com.bayerna.reserveforces.domain.target.TargetStatus
import com.bayerna.reserveforces.domain.user.UserAccount
import com.bayerna.reserveforces.repository.AttendanceRepository
import com.bayerna.reserveforces.repository.MobilizationTargetRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Duration
import java.time.OffsetDateTime
import java.util.UUID

/**
 * FR-MOB-003(입영 상태 변경), FR-RULE-002~004(지연입소·조기퇴소·예외 판정)을 오케스트레이션한다.
 * 입영 시각이 변경될 때마다 관련 Rule Engine을 다시 실행해 판정 결과를 갱신한다
 * (요구사항 정의서 13절 수용 기준 #3).
 */
@Service
class AttendanceService(
    private val mobilizationTargetRepository: MobilizationTargetRepository,
    private val attendanceRepository: AttendanceRepository,
    private val ruleEvaluationService: RuleEvaluationService,
    private val auditLogService: AuditLogService,
) {

    @Transactional
    fun recordAttendance(
        targetId: UUID,
        arrivedAt: OffsetDateTime?,
        distanceKm: BigDecimal?,
        actor: UserAccount?,
    ): Attendance {
        val target = getTarget(targetId)
        val beforeStatus = target.targetStatus

        val attendance = attendanceRepository.findByTarget_Id(targetId)
            .orElseGet { Attendance(target = target, scheduledAt = target.mobilization.scheduledStartAt) }

        attendance.arrivedAt = arrivedAt
        if (distanceKm != null) {
            attendance.distanceKm = distanceKm
        }
        attendance.recordedBy = actor

        // 재판정을 위해 기존 판정 이력을 지우고 최신 입력값으로 Rule Engine을 다시 실행한다.
        ruleEvaluationService.clearForTarget(targetId)

        var hasException = false

        if (arrivedAt != null) {
            val delayMinutes = Duration.between(attendance.scheduledAt, arrivedAt).toMinutes().toDouble()
            val evaluation = ruleEvaluationService.evaluate(
                target, RuleType.TIME, mapOf("arrival_delay_minutes" to delayMinutes), actor,
            )
            attendance.attendanceStatus = when {
                evaluation == null -> AttendanceStatus.EXCEPTION.also { hasException = true }
                evaluation.resultCode == "DELAY" -> AttendanceStatus.DELAY
                else -> AttendanceStatus.NORMAL
            }
        } else {
            attendance.attendanceStatus = AttendanceStatus.PENDING
        }

        attendance.distanceKm?.let { distance ->
            val evaluation = ruleEvaluationService.evaluate(
                target, RuleType.DISTANCE, mapOf("distance_km" to distance.toDouble()), actor,
            )
            if (evaluation == null) {
                hasException = true
            }
        }

        attendanceRepository.save(attendance)

        target.targetStatus = when {
            hasException -> TargetStatus.EXCEPTION
            arrivedAt == null -> TargetStatus.EXPECTED
            attendance.attendanceStatus == AttendanceStatus.DELAY -> TargetStatus.DELAYED
            else -> TargetStatus.ARRIVED
        }
        mobilizationTargetRepository.save(target)

        auditLogService.record(
            actor = actor,
            action = "ATTENDANCE_RECORDED",
            targetType = "mobilization_target",
            targetId = targetId,
            before = mapOf("targetStatus" to beforeStatus.name),
            after = mapOf("targetStatus" to target.targetStatus.name, "attendanceStatus" to attendance.attendanceStatus.name),
        )

        return attendance
    }

    @Transactional
    fun markAbsent(targetId: UUID, actor: UserAccount?): MobilizationTarget {
        val target = getTarget(targetId)
        val before = target.targetStatus
        target.targetStatus = TargetStatus.ABSENT
        mobilizationTargetRepository.save(target)

        attendanceRepository.findByTarget_Id(targetId).ifPresent {
            it.attendanceStatus = AttendanceStatus.PENDING
            it.arrivedAt = null
            attendanceRepository.save(it)
        }

        auditLogService.record(
            actor = actor,
            action = "TARGET_MARKED_ABSENT",
            targetType = "mobilization_target",
            targetId = targetId,
            before = mapOf("targetStatus" to before.name),
            after = mapOf("targetStatus" to target.targetStatus.name),
        )
        return target
    }

    /** FR-RULE-004: 규칙 적용이 불가능하거나 추가 확인이 필요한 경우 담당자가 직접 예외로 분류한다. */
    @Transactional
    fun markException(targetId: UUID, reason: String, actor: UserAccount?): MobilizationTarget {
        val target = getTarget(targetId)
        val before = target.targetStatus
        target.targetStatus = TargetStatus.EXCEPTION
        mobilizationTargetRepository.save(target)

        auditLogService.record(
            actor = actor,
            action = "TARGET_MARKED_EXCEPTION",
            targetType = "mobilization_target",
            targetId = targetId,
            before = mapOf("targetStatus" to before.name),
            after = mapOf("targetStatus" to target.targetStatus.name, "reason" to reason),
        )
        return target
    }

    @Transactional
    fun completeTarget(targetId: UUID, actor: UserAccount?): MobilizationTarget {
        val target = getTarget(targetId)
        val before = target.targetStatus
        target.targetStatus = TargetStatus.COMPLETED
        mobilizationTargetRepository.save(target)

        auditLogService.record(
            actor = actor,
            action = "TARGET_COMPLETED",
            targetType = "mobilization_target",
            targetId = targetId,
            before = mapOf("targetStatus" to before.name),
            after = mapOf("targetStatus" to target.targetStatus.name),
        )
        return target
    }

    fun findAttendance(targetId: UUID) = attendanceRepository.findByTarget_Id(targetId)

    /** FR-RULE-001: 저장된 입력값을 그대로 사용해 Rule Engine을 다시 실행한다 (규정/규칙 변경 후 재판정용). */
    @Transactional
    fun reevaluate(targetId: UUID, actor: UserAccount?): Attendance {
        val existing = attendanceRepository.findByTarget_Id(targetId)
            .orElseThrow { ApiException.notFound("입영 기록이 없어 판정을 실행할 수 없습니다: $targetId") }
        return recordAttendance(targetId, existing.arrivedAt, existing.distanceKm, actor)
    }

    private fun getTarget(targetId: UUID): MobilizationTarget =
        mobilizationTargetRepository.findById(targetId)
            .orElseThrow { ApiException.notFound("소집 대상을 찾을 수 없습니다: $targetId") }
}
