package com.bayerna.reserveforces.domain.location

import com.bayerna.reserveforces.common.TimestampedEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.UUID

/** locations (소집/훈련 장소). docs/db-design.md 5.3 참조. */
@Entity
@Table(name = "locations")
class Location(
    @Column(name = "name", nullable = false, length = 100)
    var name: String,

    @Column(name = "address", length = 255)
    var address: String? = null,

    @Column(name = "latitude", precision = 9, scale = 6)
    var latitude: BigDecimal? = null,

    @Column(name = "longitude", precision = 9, scale = 6)
    var longitude: BigDecimal? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", nullable = false, length = 20)
    var locationType: LocationType,

    id: UUID = UUID.randomUUID(),
) : TimestampedEntity(id)
