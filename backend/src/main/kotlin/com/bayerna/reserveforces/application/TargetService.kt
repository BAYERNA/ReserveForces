package com.bayerna.reserveforces.application

import com.bayerna.reserveforces.common.ApiException
import com.bayerna.reserveforces.domain.attendance.Attendance
import com.bayerna.reserveforces.domain.target.MobilizationTarget
import com.bayerna.reserveforces.domain.target.TargetStatus
import com.bayerna.reserveforces.repository.AttendanceRepository
import com.bayerna.reserveforces.repository.MobilizationTargetRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Optional
import java.util.UUID

/** FR-DASH-003/004: 소집 대상자 검색 및 상세 조회. */
@Service
@Transactional(readOnly = true)
class TargetService(
    private val mobilizationTargetRepository: MobilizationTargetRepository,
    private val attendanceRepository: AttendanceRepository,
) {

    fun list(mobilizationId: UUID?, status: TargetStatus?, query: String?): List<MobilizationTarget> {
        var targets = when {
            mobilizationId != null && status != null ->
                mobilizationTargetRepository.findByMobilization_IdAndTargetStatus(mobilizationId, status)
            mobilizationId != null -> mobilizationTargetRepository.findByMobilization_Id(mobilizationId)
            else -> mobilizationTargetRepository.findAll()
        }
        if (mobilizationId == null && status != null) {
            targets = targets.filter { it.targetStatus == status }
        }
        if (!query.isNullOrBlank()) {
            targets = targets.filter {
                it.reservist.name.contains(query) || it.reservist.demoIdentifier.contains(query, ignoreCase = true)
            }
        }
        return targets
    }

    fun getDetail(targetId: UUID): MobilizationTarget =
        mobilizationTargetRepository.findById(targetId)
            .orElseThrow { ApiException.notFound("소집 대상을 찾을 수 없습니다: $targetId") }

    fun findAttendance(targetId: UUID): Optional<Attendance> = attendanceRepository.findByTarget_Id(targetId)
}
