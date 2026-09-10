package com.bayerna.reserveforces.domain.entry;

import com.bayerna.reserveforces.domain.reservist.Reservist;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 입영기록 (ENTRY_RECORD). docs/db-design.md 참조. */
@Entity
@Table(name = "entry_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "entry_id")
    private Long entryId;

    @ManyToOne
    @JoinColumn(name = "reservist_id", nullable = false)
    private Reservist reservist;

    @Column(name = "actual_entry_datetime")
    private LocalDateTime actualEntryDatetime;

    @Column(name = "entry_status", nullable = false, length = 20)
    private EntryStatus entryStatus;
}
