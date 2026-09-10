package com.bayerna.reserveforces.domain.mobilization

import com.bayerna.reserveforces.common.TimestampedEntity
import com.bayerna.reserveforces.domain.location.Location
import com.bayerna.reserveforces.domain.unit.MilitaryUnit
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

/** mobilizations (소집 회차). docs/db-design.md 5.5 참조. */
@Entity
@Table(name = "mobilizations")
class Mobilization(
    @ManyToOne
    @JoinColumn(name = "unit_id", nullable = false)
    var unit: MilitaryUnit,

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = false)
    var location: Location,

    @Column(name = "name", nullable = false, length = 100)
    var name: String,

    @Column(name = "scheduled_start_at", nullable = false)
    var scheduledStartAt: OffsetDateTime,

    @Column(name = "scheduled_end_at")
    var scheduledEndAt: OffsetDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: MobilizationStatus = MobilizationStatus.PLANNED,

    id: UUID = UUID.randomUUID(),
) : TimestampedEntity(id)
