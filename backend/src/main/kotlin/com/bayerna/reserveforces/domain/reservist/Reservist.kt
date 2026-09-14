package com.bayerna.reserveforces.domain.reservist

import com.bayerna.reserveforces.common.ActiveStatus
import com.bayerna.reserveforces.common.TimestampedEntity
import com.bayerna.reserveforces.domain.user.UserAccount
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

/** reservists (예비군, 시연용 가명 데이터). docs/db-design.md 5.4 참조. */
@Entity
@Table(name = "reservists")
class Reservist(
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    var user: UserAccount,

    @Column(name = "demo_identifier", nullable = false, unique = true, length = 30)
    var demoIdentifier: String,

    @Column(name = "name", nullable = false, length = 50)
    var name: String,

    @Column(name = "address_region", length = 100)
    var addressRegion: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: ActiveStatus = ActiveStatus.ACTIVE,

    id: UUID = UUID.randomUUID(),
) : TimestampedEntity(id)
