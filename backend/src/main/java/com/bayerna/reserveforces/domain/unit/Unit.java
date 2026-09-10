package com.bayerna.reserveforces.domain.unit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 소집부대/훈련장 (UNIT). docs/db-design.md 참조.
 */
@Entity
@Table(name = "unit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "unit_id")
    private Long unitId;

    @Column(name = "unit_name", nullable = false, length = 100)
    private String unitName;

    @Column(name = "location", nullable = false, length = 200)
    private String location;

    @Column(name = "entry_deadline_time", nullable = false)
    private LocalTime entryDeadlineTime;

    @Column(name = "contact", length = 50)
    private String contact;
}
