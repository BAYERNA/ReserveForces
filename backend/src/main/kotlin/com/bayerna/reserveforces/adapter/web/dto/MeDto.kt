package com.bayerna.reserveforces.adapter.web.dto

import com.bayerna.reserveforces.domain.attendance.Attendance
import com.bayerna.reserveforces.domain.attendance.AttendanceStatus
import com.bayerna.reserveforces.domain.evaluation.RuleEvaluation
import com.bayerna.reserveforces.domain.target.MobilizationTarget
import com.bayerna.reserveforces.domain.target.TargetStatus
import java.time.OffsetDateTime
import java.util.UUID

/** FR-MOB-004: 예비군 본인 소집 정보 (SCR-05/06). */
data class MyMobilizationResponse(
    val targetId: UUID,
    val mobilizationName: String,
    val unitName: String,
    val locationName: String,
    val locationAddress: String?,
    val scheduledStartAt: OffsetDateTime,
    val noticeConfirmedAt: OffsetDateTime?,
) {
    companion object {
        fun from(target: MobilizationTarget) = MyMobilizationResponse(
            targetId = target.id,
            mobilizationName = target.mobilization.name,
            unitName = target.mobilization.unit.name,
            locationName = target.mobilization.location.name,
            locationAddress = target.mobilization.location.address,
            scheduledStartAt = target.mobilization.scheduledStartAt,
            noticeConfirmedAt = target.noticeConfirmedAt,
        )
    }
}

/** FR-MOB-005: 예비군 본인 입영 처리 상태 및 판정 결과 (SCR-06). */
data class MyStatusResponse(
    val targetStatus: TargetStatus,
    val attendanceStatus: AttendanceStatus?,
    val arrivedAt: OffsetDateTime?,
    val evaluations: List<RuleEvaluationResponse>,
) {
    companion object {
        fun from(target: MobilizationTarget, attendance: Attendance?, evaluations: List<RuleEvaluation>) =
            MyStatusResponse(
                targetStatus = target.targetStatus,
                attendanceStatus = attendance?.attendanceStatus,
                arrivedAt = attendance?.arrivedAt,
                evaluations = evaluations.map(RuleEvaluationResponse::from),
            )
    }
}
