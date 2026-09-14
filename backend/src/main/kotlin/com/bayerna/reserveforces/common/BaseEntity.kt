package com.bayerna.reserveforces.common

import jakarta.persistence.Column
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime
import java.util.UUID

/**
 * 모든 엔티티의 공통 식별자. ID는 UUID 기반으로 애플리케이션에서 직접 생성한다
 * (docs/db-design.md 12절 JPA 엔티티 설계 방향).
 */
@MappedSuperclass
abstract class BaseEntity(
    @Id
    @Column(nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),
)

/** created_at/updated_at을 공통으로 관리하는 테이블에서 사용하는 상위 클래스. */
@MappedSuperclass
abstract class TimestampedEntity(
    id: UUID = UUID.randomUUID(),
) : BaseEntity(id) {

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime? = null
        protected set

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime? = null
        protected set
}
