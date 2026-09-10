package com.bayerna.reserveforces.domain.reservist;

import com.bayerna.reserveforces.domain.unit.Unit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 소집대상자 (RESERVIST). docs/db-design.md 참조.
 * 조기퇴소 판정 기준(거주지-소집부대 거리 100km 이상)은
 * {@link com.bayerna.reserveforces.domain.judgment.JudgmentRuleEngine}에서 사용한다.
 */
@Entity
@Table(name = "reservist")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservist_id")
    private Long reservistId;

    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    @Column(name = "residence_distance_km", nullable = false, precision = 6, scale = 1)
    private BigDecimal residenceDistanceKm;

    @ManyToOne
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @Column(name = "status", nullable = false, length = 20)
    private ReservistStatus status;
}
