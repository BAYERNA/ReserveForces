package com.bayerna.reserveforces.domain.attendance

import com.bayerna.reserveforces.common.TimestampedEntity
import com.bayerna.reserveforces.domain.target.MobilizationTarget
import com.bayerna.reserveforces.domain.user.UserAccount
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

/** attendances (입영 사실 기록). docs/db-design.md 5.7 참조. */
@Entity
@Table(name = "attendances")
class Attendance(
    @ManyToOne
    @JoinColumn(name = "target_id", nullable = false, unique = true)
    var target: MobilizationTarget,

    @Column(name = "scheduled_at", nullable = false)
    var scheduledAt: OffsetDateTime,

    @Column(name = "arrived_at")
    var arrivedAt: OffsetDateTime? = null,

    @Column(name = "departure_at")
    var departureAt: OffsetDateTime? = null,

    @Column(name = "distance_km", precision = 8, scale = 2)
    var distanceKm: BigDecimal? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status", nullable = false, length = 30)
    var attendanceStatus: AttendanceStatus = AttendanceStatus.PENDING,

    @ManyToOne
    @JoinColumn(name = "recorded_by")
    var recordedBy: UserAccount? = null,

    id: UUID = UUID.randomUUID(),
) : TimestampedEntity(id)
