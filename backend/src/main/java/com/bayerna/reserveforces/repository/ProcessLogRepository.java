package com.bayerna.reserveforces.repository;

import com.bayerna.reserveforces.domain.processlog.ProcessLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessLogRepository extends JpaRepository<ProcessLog, Long> {

    List<ProcessLog> findByJudgment_JudgmentIdOrderByProcessedAtDesc(Long judgmentId);

    List<ProcessLog> findAllByOrderByProcessedAtDesc();
}
