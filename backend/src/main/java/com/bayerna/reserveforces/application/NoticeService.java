package com.bayerna.reserveforces.application;

import com.bayerna.reserveforces.common.ApiException;
import com.bayerna.reserveforces.domain.notice.CallupNotice;
import com.bayerna.reserveforces.domain.reservist.Reservist;
import com.bayerna.reserveforces.domain.unit.Unit;
import com.bayerna.reserveforces.repository.CallupNoticeRepository;
import com.bayerna.reserveforces.repository.ReservistRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** FR-07, FR-08: 예비군 본인의 소집통지 및 입영 부대·훈련장 정보 조회. */
@Service
@Transactional(readOnly = true)
public class NoticeService {

    private final CallupNoticeRepository callupNoticeRepository;
    private final ReservistRepository reservistRepository;

    public NoticeService(CallupNoticeRepository callupNoticeRepository, ReservistRepository reservistRepository) {
        this.callupNoticeRepository = callupNoticeRepository;
        this.reservistRepository = reservistRepository;
    }

    public List<CallupNotice> listNoticesForReservist(Long reservistId) {
        return callupNoticeRepository.findByReservist_ReservistIdOrderByScheduledDatetimeDesc(reservistId);
    }

    public Unit getUnitForReservist(Long reservistId) {
        Reservist reservist = reservistRepository.findById(reservistId)
                .orElseThrow(() -> ApiException.notFound("소집대상자를 찾을 수 없습니다: " + reservistId));
        return reservist.getUnit();
    }
}
