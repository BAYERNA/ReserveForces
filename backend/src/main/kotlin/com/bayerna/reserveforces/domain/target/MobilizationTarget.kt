package com.bayerna.reserveforces.domain.target

import com.bayerna.reserveforces.common.TimestampedEntity
import com.bayerna.reserveforces.domain.mobilization.Mobilization
import com.bayerna.reserveforces.domain.reservist.Reservist
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

/** mobilization_targets (소집 대상). docs/db-design.md 5.6 참조. */
@Entity
@Table(name = "mobilization_targets")
class MobilizationTarget(
    @ManyToOne
    @JoinColumn(name = "mobilization_id", nullable = false)
    var mobilization: Mobilization,

    @ManyToOne
    @JoinColumn(name = "reservist_id", nullable = false)
    var reservist: Reservist,

    @Enumerated(EnumType.STRING)
    @Column(name = "target_status", nullable = false, length = 30)
    var targetStatus: TargetStatus = TargetStatus.EXPECTED,

    @Column(name = "notice_confirmed_at")
    var noticeConfirmedAt: OffsetDateTime? = null,

    id: UUID = UUID.randomUUID(),
) : TimestampedEntity(id)
