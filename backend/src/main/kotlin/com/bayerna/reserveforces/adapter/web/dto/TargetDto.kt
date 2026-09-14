package com.bayerna.reserveforces.adapter.web.dto

import com.bayerna.reserveforces.domain.attendance.Attendance
import com.bayerna.reserveforces.domain.attendance.AttendanceStatus
import com.bayerna.reserveforces.domain.target.MobilizationTarget
import com.bayerna.reserveforces.domain.target.TargetStatus
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

data class TargetResponse(
    val targetId: UUID,
    val mobilizationId: UUID,
    val mobilizationName: String,
    val unitName: String,
    val locationName: String,
    val reservistId: UUID,
    val reservistName: String,
    val demoIdentifier: String,
    val addressRegion: String?,
    val targetStatus: TargetStatus,
    val scheduledAt: OffsetDateTime?,
    val arrivedAt: OffsetDateTime?,
    val distanceKm: BigDecimal?,
    val attendanceStatus: AttendanceStatus?,
) {
    companion object {
        fun from(target: MobilizationTarget, attendance: Attendance?) = TargetResponse(
            targetId = target.id,
            mobilizationId = target.mobilization.id,
            mobilizationName = target.mobilization.name,
            unitName = target.mobilization.unit.name,
            locationName = target.mobilization.location.name,
            reservistId = target.reservist.id,
            reservistName = target.reservist.name,
            demoIdentifier = target.reservist.demoIdentifier,
            addressRegion = target.reservist.addressRegion,
            targetStatus = target.targetStatus,
            scheduledAt = attendance?.scheduledAt,
            arrivedAt = attendance?.arrivedAt,
            distanceKm = attendance?.distanceKm,
            attendanceStatus = attendance?.attendanceStatus,
        )
    }
}

data class RecordAttendanceRequest(
    val arrivedAt: OffsetDateTime?,
    @field:DecimalMin(value = "0.0", message = "거주지 거리는 0km 이상이어야 합니다.")
    @field:Digits(integer = 6, fraction = 2, message = "거주지 거리 값이 올바르지 않습니다.")
    val distanceKm: BigDecimal?,
)

data class MarkExceptionRequest(
    @field:NotBlank(message = "예외 처리 사유를 입력해 주세요.") val reason: String,
)
