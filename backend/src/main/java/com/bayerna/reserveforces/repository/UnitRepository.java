package com.bayerna.reserveforces.repository;

import com.bayerna.reserveforces.domain.unit.Unit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnitRepository extends JpaRepository<Unit, Long> {
}
