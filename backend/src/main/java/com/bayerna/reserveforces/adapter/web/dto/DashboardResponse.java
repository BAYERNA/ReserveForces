package com.bayerna.reserveforces.adapter.web.dto;

import com.bayerna.reserveforces.application.ReservistQueryService.DashboardStats;
import com.bayerna.reserveforces.domain.reservist.ReservistStatus;
import java.util.Map;

public record DashboardResponse(
        long totalReservists,
        long enteredCount,
        double entryCompletionRate,
        Map<ReservistStatus, Long> statusCounts,
        long lateEntryCount,
        long noShowCount) {

    public static DashboardResponse from(DashboardStats stats) {
        return new DashboardResponse(
                stats.totalReservists(),
                stats.enteredCount(),
                stats.entryCompletionRate(),
                stats.statusCounts(),
                stats.lateEntryCount(),
                stats.noShowCount());
    }
}
