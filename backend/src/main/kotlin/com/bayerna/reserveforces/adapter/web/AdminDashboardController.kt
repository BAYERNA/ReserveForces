package com.bayerna.reserveforces.adapter.web

import com.bayerna.reserveforces.adapter.web.dto.DashboardResponse
import com.bayerna.reserveforces.application.DashboardService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/** FR-DASH-001/002: 통합 대시보드. */
@RestController
@RequestMapping("/api/v1/admin/dashboard")
class AdminDashboardController(private val dashboardService: DashboardService) {

    @GetMapping
    fun getDashboard(@RequestParam(required = false) mobilizationId: UUID?): DashboardResponse =
        DashboardResponse.from(dashboardService.getStats(mobilizationId))
}
