package com.bayerna.reserveforces.repository;

import com.bayerna.reserveforces.domain.entry.EntryRecord;
import com.bayerna.reserveforces.domain.entry.EntryStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntryRecordRepository extends JpaRepository<EntryRecord, Long> {

    List<EntryRecord> findByReservist_ReservistId(Long reservistId);

    long countByEntryStatus(EntryStatus entryStatus);
}
