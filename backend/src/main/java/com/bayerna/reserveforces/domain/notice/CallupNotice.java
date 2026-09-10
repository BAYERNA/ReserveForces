package com.bayerna.reserveforces.domain.notice;

import com.bayerna.reserveforces.domain.reservist.Reservist;
import com.bayerna.reserveforces.domain.unit.Unit;
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

/** 소집통지 (CALLUP_NOTICE). docs/db-design.md 참조. */
@Entity
@Table(name = "callup_notice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallupNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long noticeId;

    @ManyToOne
    @JoinColumn(name = "reservist_id", nullable = false)
    private Reservist reservist;

    @ManyToOne
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @Column(name = "scheduled_datetime", nullable = false)
    private LocalDateTime scheduledDatetime;

    @Column(name = "notice_status", nullable = false, length = 20)
    private NoticeStatus noticeStatus;
}
