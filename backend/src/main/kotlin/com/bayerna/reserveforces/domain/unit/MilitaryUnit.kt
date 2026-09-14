package com.bayerna.reserveforces.domain.unit

import com.bayerna.reserveforces.common.ActiveStatus
import com.bayerna.reserveforces.common.TimestampedEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.util.UUID

/**
 * units (소집부대). Kotlin 예약어 `Unit`과의 충돌을 피하기 위해 클래스명은 MilitaryUnit을 사용하고,
 * 테이블명은 docs/db-design.md 명세대로 `units`를 유지한다.
 */
@Entity
@Table(name = "units")
class MilitaryUnit(
    @Column(name = "code", nullable = false, unique = true, length = 30)
    var code: String,

    @Column(name = "name", nullable = false, length = 100)
    var name: String,

    @Column(name = "region_code", length = 20)
    var regionCode: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: ActiveStatus = ActiveStatus.ACTIVE,

    id: UUID = UUID.randomUUID(),
) : TimestampedEntity(id)
