package com.bayerna.reserveforces.adapter.web;

import com.bayerna.reserveforces.adapter.web.dto.RecordEntryRequest;
import com.bayerna.reserveforces.adapter.web.dto.ReservistResponse;
import com.bayerna.reserveforces.application.JudgmentService;
import com.bayerna.reserveforces.application.ReservistQueryService;
import com.bayerna.reserveforces.domain.reservist.ReservistStatus;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** FR-02: 소집대상자 현황 조회, FR-04: 지연입소 자동판정(입영 처리) */
@RestController
@RequestMapping("/api/admin/reservists")
public class AdminReservistController {

    private final ReservistQueryService reservistQueryService;
    private final JudgmentService judgmentService;

    public AdminReservistController(ReservistQueryService reservistQueryService, JudgmentService judgmentService) {
        this.reservistQueryService = reservistQueryService;
        this.judgmentService = judgmentService;
    }

    @GetMapping
    public List<ReservistResponse> listReservists(
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) ReservistStatus status) {
        return reservistQueryService.listReservists(unitId, status).stream()
                .map(reservist -> ReservistResponse.from(
                        reservist, reservistQueryService.findLatestEntryRecord(reservist.getReservistId()).orElse(null)))
                .toList();
    }

    /** 실제 입영 일시를 입력받아 지연입소 여부를 룰엔진으로 즉시 자동판정한다. actualEntryDatetime이 null이면 미입영 처리. */
    @PostMapping("/{reservistId}/entry")
    public ReservistResponse recordEntry(
            @PathVariable Long reservistId, @RequestBody RecordEntryRequest request) {
        var entryRecord = judgmentService.recordEntryAndJudgeLateEntry(reservistId, request.actualEntryDatetime());
        return ReservistResponse.from(entryRecord.getReservist(), entryRecord);
    }
}
