package com.bayerna.reserveforces.application

import com.bayerna.reserveforces.common.ApiException
import com.bayerna.reserveforces.domain.attendance.Attendance
import com.bayerna.reserveforces.domain.evaluation.RuleEvaluation
import com.bayerna.reserveforces.domain.target.MobilizationTarget
import com.bayerna.reserveforces.repository.AttendanceRepository
import com.bayerna.reserveforces.repository.MobilizationTargetRepository
import com.bayerna.reserveforces.repository.RuleEvaluationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * FR-MOB-004/005: 예비군 본인 소집 정보 및 처리 상태 조회.
 * 항상 JWT의 reservistId만을 조회 조건으로 사용하여 타인의 정보에 접근할 수 없도록 한다 (NFR-SEC-002).
 */
@Service
@Transactional(readOnly = true)
class MeService(
    private val mobilizationTargetRepository: MobilizationTargetRepository,
    private val attendanceRepository: AttendanceRepository,
    private val ruleEvaluationRepository: RuleEvaluationRepository,
) {

    fun getLatestTarget(reservistId: UUID): MobilizationTarget =
        mobilizationTargetRepository.findTopByReservist_IdOrderByCreatedAtDesc(reservistId)
            .orElseThrow { ApiException.notFound("본인에게 발송된 소집통지가 없습니다.") }

    fun getAttendance(targetId: UUID): Attendance? = attendanceRepository.findByTarget_Id(targetId).orElse(null)

    fun getEvaluations(targetId: UUID): List<RuleEvaluation> =
        ruleEvaluationRepository.findByTarget_IdOrderByEvaluatedAtDesc(targetId)
}
