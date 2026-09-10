package com.bayerna.reserveforces.application

import com.bayerna.reserveforces.domain.attendance.AttendanceStatus
import com.bayerna.reserveforces.domain.evaluation.RuleEvaluation
import com.bayerna.reserveforces.domain.location.Location
import com.bayerna.reserveforces.domain.location.LocationType
import com.bayerna.reserveforces.domain.mobilization.Mobilization
import com.bayerna.reserveforces.domain.reservist.Reservist
import com.bayerna.reserveforces.domain.rule.Rule
import com.bayerna.reserveforces.domain.rule.RuleType
import com.bayerna.reserveforces.domain.target.MobilizationTarget
import com.bayerna.reserveforces.domain.target.TargetStatus
import com.bayerna.reserveforces.domain.unit.MilitaryUnit
import com.bayerna.reserveforces.domain.user.UserAccount
import com.bayerna.reserveforces.domain.user.UserRole
import com.bayerna.reserveforces.repository.AttendanceRepository
import com.bayerna.reserveforces.repository.MobilizationTargetRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.OffsetDateTime
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * FR-RULE-002~004: 입영 시각 변경 시 Rule Engine 재실행 결과에 따라
 * targetStatus/attendanceStatus가 올바르게 전이되는지 검증한다 (요구사항 정의서 13절 수용 기준 #3).
 */
class AttendanceServiceTest {

    private lateinit var mobilizationTargetRepository: MobilizationTargetRepository
    private lateinit var attendanceRepository: AttendanceRepository
    private lateinit var ruleEvaluationService: RuleEvaluationService
    private lateinit var auditLogService: AuditLogService
    private lateinit var attendanceService: AttendanceService
    private lateinit var target: MobilizationTarget

    /** Mockito의 any()는 null을 반환해 Kotlin의 non-null 파라미터 검사에서 NPE를 유발하므로 우회한다. */
    private fun <T> any(): T {
        Mockito.any<T>()
        @Suppress("UNCHECKED_CAST")
        return null as T
    }

    @BeforeEach
    fun setUp() {
        mobilizationTargetRepository = mock(MobilizationTargetRepository::class.java)
        attendanceRepository = mock(AttendanceRepository::class.java)
        ruleEvaluationService = mock(RuleEvaluationService::class.java)
        auditLogService = mock(AuditLogService::class.java)
        attendanceService = AttendanceService(
            mobilizationTargetRepository, attendanceRepository, ruleEvaluationService, auditLogService,
        )

        val unit = MilitaryUnit(code = "U1", name = "제1보충대대")
        val location = Location(name = "입영장", locationType = LocationType.MOBILIZATION_SITE)
        val mobilization = Mobilization(
            unit = unit, location = location, name = "테스트 소집",
            scheduledStartAt = OffsetDateTime.parse("2026-09-10T09:00:00Z"),
        )
        val user = mock(UserAccount::class.java)
        `when`(user.role).thenReturn(UserRole.RESERVIST)
        val reservist = Reservist(user = user, demoIdentifier = "RSV-0001", name = "홍길동")
        target = MobilizationTarget(mobilization = mobilization, reservist = reservist)

        `when`(mobilizationTargetRepository.findById(target.id)).thenReturn(Optional.of(target))
        `when`(attendanceRepository.findByTarget_Id(target.id)).thenReturn(Optional.empty())
        `when`(mobilizationTargetRepository.save(any())).thenAnswer { it.arguments[0] }
        `when`(attendanceRepository.save(any())).thenAnswer { it.arguments[0] }
    }

    private fun stubEvaluation(resultCode: String): RuleEvaluation {
        val rule = mock(Rule::class.java)
        `when`(rule.version).thenReturn("v1")
        `when`(rule.resultCode).thenReturn(resultCode)
        `when`(rule.name).thenReturn("테스트 규칙")
        return RuleEvaluation(
            target = target, rule = rule, ruleVersion = "v1",
            inputData = "{}", resultCode = resultCode,
        )
    }

    @Test
    fun `정시 입영 시 NORMAL 판정과 ARRIVED 상태로 전이한다`() {
        val evaluation = stubEvaluation("NORMAL")
        `when`(ruleEvaluationService.evaluate(any(), any(), anyMap(), any())).thenReturn(evaluation)

        val attendance = attendanceService.recordAttendance(
            target.id, OffsetDateTime.parse("2026-09-10T09:10:00Z"), null, null,
        )

        assertEquals(AttendanceStatus.NORMAL, attendance.attendanceStatus)
        assertEquals(TargetStatus.ARRIVED, target.targetStatus)
    }

    @Test
    fun `지연 판정 시 DELAY 상태와 DELAYED 대상 상태로 전이한다`() {
        val evaluation = stubEvaluation("DELAY")
        `when`(ruleEvaluationService.evaluate(any(), any(), anyMap(), any())).thenReturn(evaluation)

        val attendance = attendanceService.recordAttendance(
            target.id, OffsetDateTime.parse("2026-09-10T10:30:00Z"), null, null,
        )

        assertEquals(AttendanceStatus.DELAY, attendance.attendanceStatus)
        assertEquals(TargetStatus.DELAYED, target.targetStatus)
    }

    @Test
    fun `일치하는 규칙이 없으면 EXCEPTION으로 분류한다`() {
        `when`(ruleEvaluationService.evaluate(any(), any(), anyMap(), any())).thenReturn(null)

        val attendance = attendanceService.recordAttendance(
            target.id, OffsetDateTime.parse("2026-09-10T09:10:00Z"), null, null,
        )

        assertEquals(AttendanceStatus.EXCEPTION, attendance.attendanceStatus)
        assertEquals(TargetStatus.EXCEPTION, target.targetStatus)
    }

    @Test
    fun `입영 일시가 없으면 PENDING 상태를 유지하고 EXPECTED로 전이한다`() {
        val attendance = attendanceService.recordAttendance(target.id, null, null, null)

        assertEquals(AttendanceStatus.PENDING, attendance.attendanceStatus)
        assertEquals(TargetStatus.EXPECTED, target.targetStatus)
    }

    @Test
    fun `미입영 처리 시 ABSENT 상태로 전이하고 입영 기록을 초기화한다`() {
        val existingAttendance = com.bayerna.reserveforces.domain.attendance.Attendance(
            target = target, scheduledAt = target.mobilization.scheduledStartAt,
            arrivedAt = OffsetDateTime.now(), attendanceStatus = AttendanceStatus.NORMAL,
        )
        `when`(attendanceRepository.findByTarget_Id(target.id)).thenReturn(Optional.of(existingAttendance))

        attendanceService.markAbsent(target.id, null)

        assertEquals(TargetStatus.ABSENT, target.targetStatus)
        assertEquals(AttendanceStatus.PENDING, existingAttendance.attendanceStatus)
        assertNull(existingAttendance.arrivedAt)
    }

    @Test
    fun `완료 처리 시 COMPLETED 상태로 전이한다`() {
        val result = attendanceService.completeTarget(target.id, null)
        assertEquals(TargetStatus.COMPLETED, result.targetStatus)
    }

    @Test
    fun `예외 처리 시 EXCEPTION 상태로 전이한다`() {
        val result = attendanceService.markException(target.id, "거리 데이터 누락", null)
        assertNotNull(result)
        assertEquals(TargetStatus.EXCEPTION, result.targetStatus)
    }
}
