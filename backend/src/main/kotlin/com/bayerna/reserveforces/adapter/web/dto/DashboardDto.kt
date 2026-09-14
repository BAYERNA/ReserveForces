package com.bayerna.reserveforces.adapter.web.dto

import com.bayerna.reserveforces.application.DashboardStats
import com.bayerna.reserveforces.domain.target.TargetStatus

data class DashboardResponse(
    val totalTargets: Long,
    val arrivedCount: Long,
    val entryCompletionRate: Double,
    val statusCounts: Map<TargetStatus, Long>,
) {
    companion object {
        fun from(stats: DashboardStats) =
            DashboardResponse(stats.totalTargets, stats.arrivedCount, stats.entryCompletionRate, stats.statusCounts)
    }
}
