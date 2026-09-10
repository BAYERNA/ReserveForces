package com.bayerna.reserveforces.repository;

import com.bayerna.reserveforces.domain.judgment.JudgmentResult;
import com.bayerna.reserveforces.domain.judgment.JudgmentType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JudgmentResultRepository extends JpaRepository<JudgmentResult, Long> {

    List<JudgmentResult> findByReservist_ReservistId(Long reservistId);

    Optional<JudgmentResult> findByReservist_ReservistIdAndJudgmentType(Long reservistId, JudgmentType judgmentType);

    List<JudgmentResult> findAllByOrderByJudgedAtDesc();
}
