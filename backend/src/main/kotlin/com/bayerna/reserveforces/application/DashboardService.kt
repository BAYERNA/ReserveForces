package com.bayerna.reserveforces.application

import com.bayerna.reserveforces.domain.target.TargetStatus
import com.bayerna.reserveforces.repository.MobilizationTargetRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/** FR-DASH-001/002: 통합 대시보드 KPI 및 입영 진행률. */
@Service
@Transactional(readOnly = true)
class DashboardService(
    private val mobilizationTargetRepository: MobilizationTargetRepository,
) {

    fun getStats(mobilizationId: UUID?): DashboardStats {
        val targets = if (mobilizationId != null) {
            mobilizationTargetRepository.findByMobilization_Id(mobilizationId)
        } else {
            mobilizationTargetRepository.findAll()
        }

        val statusCounts = TargetStatus.entries.associateWith { status ->
            targets.count { it.targetStatus == status }.toLong()
        }

        val total = targets.size.toLong()
        val arrivedLike = (statusCounts[TargetStatus.ARRIVED] ?: 0L) +
            (statusCounts[TargetStatus.DELAYED] ?: 0L) +
            (statusCounts[TargetStatus.COMPLETED] ?: 0L)
        val completionRate = if (total == 0L) 0.0 else arrivedLike * 100.0 / total

        return DashboardStats(
            totalTargets = total,
            arrivedCount = arrivedLike,
            entryCompletionRate = completionRate,
            statusCounts = statusCounts,
        )
    }
}

data class DashboardStats(
    val totalTargets: Long,
    val arrivedCount: Long,
    val entryCompletionRate: Double,
    val statusCounts: Map<TargetStatus, Long>,
)
