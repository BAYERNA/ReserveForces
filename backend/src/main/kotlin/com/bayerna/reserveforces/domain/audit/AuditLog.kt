package com.bayerna.reserveforces.domain.audit

import com.bayerna.reserveforces.common.BaseEntity
import com.bayerna.reserveforces.domain.user.UserAccount
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime
import java.util.UUID

/** audit_logs (주요 행위 감사 로그). docs/db-design.md 5.12 참조. FR-AUD-001. */
@Entity
@Table(name = "audit_logs")
class AuditLog(
    @ManyToOne
    @JoinColumn(name = "actor_id")
    var actor: UserAccount? = null,

    @Column(name = "action", nullable = false, length = 50)
    var action: String,

    @Column(name = "target_type", nullable = false, length = 50)
    var targetType: String,

    @Column(name = "target_id")
    var targetId: UUID? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "before_data", columnDefinition = "jsonb")
    var beforeData: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "after_data", columnDefinition = "jsonb")
    var afterData: String? = null,

    id: UUID = UUID.randomUUID(),
) : BaseEntity(id) {

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null
        protected set
}
