package com.bayerna.reserveforces.repository;

import com.bayerna.reserveforces.domain.reservist.Reservist;
import com.bayerna.reserveforces.domain.reservist.ReservistStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservistRepository extends JpaRepository<Reservist, Long> {

    List<Reservist> findByUnit_UnitIdAndStatus(Long unitId, ReservistStatus status);

    List<Reservist> findByUnit_UnitId(Long unitId);

    List<Reservist> findByStatus(ReservistStatus status);

    long countByStatus(ReservistStatus status);
}
