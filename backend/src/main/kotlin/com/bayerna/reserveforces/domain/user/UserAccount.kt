package com.bayerna.reserveforces.domain.user

import com.bayerna.reserveforces.common.ActiveStatus
import com.bayerna.reserveforces.common.TimestampedEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

/** users (사용자계정). docs/db-design.md 5.1 참조. */
@Entity
@Table(name = "users")
class UserAccount(
    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    var loginId: String,

    @Column(name = "password_hash", nullable = false, length = 255)
    var passwordHash: String,

    @Column(name = "name", nullable = false, length = 50)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    var role: UserRole,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: ActiveStatus = ActiveStatus.ACTIVE,

    @Column(name = "last_login_at")
    var lastLoginAt: OffsetDateTime? = null,

    id: UUID = UUID.randomUUID(),
) : TimestampedEntity(id)
