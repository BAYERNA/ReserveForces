package com.bayerna.reserveforces.application;

import com.bayerna.reserveforces.domain.entry.EntryRecord;
import com.bayerna.reserveforces.domain.entry.EntryStatus;
import com.bayerna.reserveforces.domain.reservist.Reservist;
import com.bayerna.reserveforces.domain.reservist.ReservistStatus;
import com.bayerna.reserveforces.repository.EntryRecordRepository;
import com.bayerna.reserveforces.repository.ReservistRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** FR-02, FR-03: 소집대상자 현황 조회 및 입영 진행률 대시보드 집계. */
@Service
@Transactional(readOnly = true)
public class ReservistQueryService {

    private final ReservistRepository reservistRepository;
    private final EntryRecordRepository entryRecordRepository;

    public ReservistQueryService(ReservistRepository reservistRepository, EntryRecordRepository entryRecordRepository) {
        this.reservistRepository = reservistRepository;
        this.entryRecordRepository = entryRecordRepository;
    }

    /** FR-02: 소집부대·상태 조건으로 소집대상자 명단을 조회한다. */
    public List<Reservist> listReservists(Long unitId, ReservistStatus status) {
        if (unitId != null && status != null) {
            return reservistRepository.findByUnit_UnitIdAndStatus(unitId, status);
        }
        if (unitId != null) {
            return reservistRepository.findByUnit_UnitId(unitId);
        }
        if (status != null) {
            return reservistRepository.findByStatus(status);
        }
        return reservistRepository.findAll();
    }

    public Optional<EntryRecord> findLatestEntryRecord(Long reservistId) {
        return entryRecordRepository.findByReservist_ReservistId(reservistId).stream()
                .findFirst();
    }

    /** FR-03: 전체 소집대상자 대비 입영 완료 인원 비율, 상태별 인원수를 집계한다. */
    public DashboardStats getDashboardStats(Long unitId) {
        List<Reservist> targets = unitId != null
                ? reservistRepository.findByUnit_UnitId(unitId)
                : reservistRepository.findAll();

        Map<ReservistStatus, Long> statusCounts = new HashMap<>();
        for (ReservistStatus status : ReservistStatus.values()) {
            statusCounts.put(status, 0L);
        }
        for (Reservist reservist : targets) {
            statusCounts.merge(reservist.getStatus(), 1L, Long::sum);
        }

        long total = targets.size();
        long entered = statusCounts.getOrDefault(ReservistStatus.COMPLETED, 0L)
                + statusCounts.getOrDefault(ReservistStatus.LATE, 0L)
                + statusCounts.getOrDefault(ReservistStatus.EARLY_DISCHARGE, 0L);
        double completionRate = total == 0 ? 0.0 : (entered * 100.0) / total;

        long lateCount = entryRecordRepository.countByEntryStatus(EntryStatus.LATE);
        long noShowCount = entryRecordRepository.countByEntryStatus(EntryStatus.NO_SHOW);

        return new DashboardStats(total, entered, completionRate, statusCounts, lateCount, noShowCount);
    }

    public record DashboardStats(
            long totalReservists,
            long enteredCount,
            double entryCompletionRate,
            Map<ReservistStatus, Long> statusCounts,
            long lateEntryCount,
            long noShowCount) {
    }
}
