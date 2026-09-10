package com.bayerna.reserveforces.application;

import com.bayerna.reserveforces.common.ApiException;
import com.bayerna.reserveforces.domain.entry.EntryRecord;
import com.bayerna.reserveforces.domain.entry.EntryStatus;
import com.bayerna.reserveforces.domain.judgment.JudgmentOutcome;
import com.bayerna.reserveforces.domain.judgment.JudgmentResult;
import com.bayerna.reserveforces.domain.judgment.JudgmentRuleEngine;
import com.bayerna.reserveforces.domain.judgment.JudgmentType;
import com.bayerna.reserveforces.domain.processlog.ProcessLog;
import com.bayerna.reserveforces.domain.reservist.Reservist;
import com.bayerna.reserveforces.domain.reservist.ReservistStatus;
import com.bayerna.reserveforces.domain.unit.Unit;
import com.bayerna.reserveforces.domain.user.UserAccount;
import com.bayerna.reserveforces.repository.EntryRecordRepository;
import com.bayerna.reserveforces.repository.JudgmentResultRepository;
import com.bayerna.reserveforces.repository.ProcessLogRepository;
import com.bayerna.reserveforces.repository.ReservistRepository;
import com.bayerna.reserveforces.repository.UserAccountRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 판정 관련 유스케이스. 룰엔진(FR-04, FR-05)을 실제 데이터에 적용하고,
 * 수동 보정(FR-06) 및 처리 이력(FR-10) 기록을 담당한다.
 */
@Service
@Transactional
public class JudgmentService {

    private final ReservistRepository reservistRepository;
    private final EntryRecordRepository entryRecordRepository;
    private final JudgmentResultRepository judgmentResultRepository;
    private final ProcessLogRepository processLogRepository;
    private final UserAccountRepository userAccountRepository;
    private final JudgmentRuleEngine ruleEngine;

    public JudgmentService(
            ReservistRepository reservistRepository,
            EntryRecordRepository entryRecordRepository,
            JudgmentResultRepository judgmentResultRepository,
            ProcessLogRepository processLogRepository,
            UserAccountRepository userAccountRepository,
            JudgmentRuleEngine ruleEngine) {
        this.reservistRepository = reservistRepository;
        this.entryRecordRepository = entryRecordRepository;
        this.judgmentResultRepository = judgmentResultRepository;
        this.processLogRepository = processLogRepository;
        this.userAccountRepository = userAccountRepository;
        this.ruleEngine = ruleEngine;
    }

    /**
     * FR-04: 실제 입영 일시를 기록하고, 지정 입영시간 대비 지연 여부를 룰엔진으로 자동판정한다.
     * 소집 현황 화면에서 관리자가 입영을 처리할 때 호출된다.
     */
    public EntryRecord recordEntryAndJudgeLateEntry(Long reservistId, LocalDateTime actualEntryDatetime) {
        Reservist reservist = getReservist(reservistId);
        Unit unit = reservist.getUnit();

        LocalDate baseDate = actualEntryDatetime != null ? actualEntryDatetime.toLocalDate() : LocalDate.now();
        LocalDateTime scheduledDeadline = LocalDateTime.of(baseDate, unit.getEntryDeadlineTime());

        JudgmentRuleEngine.LateEntryJudgment judgment = ruleEngine.judgeLateEntry(scheduledDeadline, actualEntryDatetime);

        EntryRecord entryRecord = entryRecordRepository.findByReservist_ReservistId(reservistId).stream()
                .findFirst()
                .orElseGet(() -> EntryRecord.builder().reservist(reservist).build());
        entryRecord.setActualEntryDatetime(actualEntryDatetime);
        entryRecord.setEntryStatus(judgment.entryStatus());
        entryRecordRepository.save(entryRecord);

        judgmentResultRepository.findByReservist_ReservistIdAndJudgmentType(reservistId, JudgmentType.LATE_ENTRY)
                .filter(JudgmentResult::isAuto)
                .ifPresent(judgmentResultRepository::delete);

        if (judgment.applicable()) {
            JudgmentResult result = JudgmentResult.builder()
                    .reservist(reservist)
                    .judgmentType(JudgmentType.LATE_ENTRY)
                    .auto(true)
                    .result(judgment.outcome())
                    .build();
            judgmentResultRepository.save(result);
        }

        reservist.setStatus(deriveReservistStatus(judgment.entryStatus()));
        reservistRepository.save(reservist);

        return entryRecord;
    }

    /** FR-05: 거주지-소집부대 거리를 기준으로 조기퇴소 허용 여부를 자동판정하여 저장한다. */
    public JudgmentResult createEarlyDischargeJudgment(Reservist reservist) {
        JudgmentRuleEngine.EarlyDischargeJudgment judgment = ruleEngine.judgeEarlyDischarge(reservist.getResidenceDistanceKm());
        JudgmentResult result = JudgmentResult.builder()
                .reservist(reservist)
                .judgmentType(JudgmentType.EARLY_DISCHARGE)
                .auto(true)
                .result(judgment.outcome())
                .build();
        return judgmentResultRepository.save(result);
    }

    public List<JudgmentResult> listAllJudgments() {
        return judgmentResultRepository.findAllByOrderByJudgedAtDesc();
    }

    public List<JudgmentResult> listJudgmentsForReservist(Long reservistId) {
        return judgmentResultRepository.findByReservist_ReservistId(reservistId);
    }

    /** FR-06: 관리자가 자동판정 결과에 예외 사유를 기재하고 수동으로 보정한다. */
    public JudgmentResult correctJudgment(Long judgmentId, JudgmentOutcome newResult, String reason, Long adminUserId) {
        JudgmentResult judgment = judgmentResultRepository.findById(judgmentId)
                .orElseThrow(() -> ApiException.notFound("판정결과를 찾을 수 없습니다: " + judgmentId));
        UserAccount admin = userAccountRepository.findById(adminUserId)
                .orElseThrow(() -> ApiException.notFound("관리자 계정을 찾을 수 없습니다: " + adminUserId));

        judgment.applyManualCorrection(newResult, reason);
        judgmentResultRepository.save(judgment);

        ProcessLog log = ProcessLog.builder()
                .judgment(judgment)
                .user(admin)
                .content("판정 결과 수동 보정: %s (사유: %s)".formatted(newResult.code(), reason))
                .build();
        processLogRepository.save(log);

        return judgment;
    }

    public List<ProcessLog> listProcessLogs() {
        return processLogRepository.findAllByOrderByProcessedAtDesc();
    }

    private Reservist getReservist(Long reservistId) {
        return reservistRepository.findById(reservistId)
                .orElseThrow(() -> ApiException.notFound("소집대상자를 찾을 수 없습니다: " + reservistId));
    }

    private ReservistStatus deriveReservistStatus(EntryStatus entryStatus) {
        return switch (entryStatus) {
            case WAITING, NO_SHOW -> ReservistStatus.BEFORE_CALLUP;
            case LATE -> ReservistStatus.LATE;
            case COMPLETED -> ReservistStatus.COMPLETED;
        };
    }
}
