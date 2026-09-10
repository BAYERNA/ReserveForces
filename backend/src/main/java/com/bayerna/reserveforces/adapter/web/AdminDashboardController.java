package com.bayerna.reserveforces.adapter.web;

import com.bayerna.reserveforces.adapter.web.dto.DashboardResponse;
import com.bayerna.reserveforces.application.ReservistQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** FR-03: 입영 진행률 대시보드 */
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final ReservistQueryService reservistQueryService;

    public AdminDashboardController(ReservistQueryService reservistQueryService) {
        this.reservistQueryService = reservistQueryService;
    }

    @GetMapping
    public DashboardResponse getDashboard(@RequestParam(required = false) Long unitId) {
        return DashboardResponse.from(reservistQueryService.getDashboardStats(unitId));
    }
}
