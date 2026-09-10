package com.bayerna.reserveforces.repository;

import com.bayerna.reserveforces.domain.notice.CallupNotice;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CallupNoticeRepository extends JpaRepository<CallupNotice, Long> {

    List<CallupNotice> findByReservist_ReservistIdOrderByScheduledDatetimeDesc(Long reservistId);
}
