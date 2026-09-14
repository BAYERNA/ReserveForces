package com.bayerna.reserveforces.domain.scenario

import com.bayerna.reserveforces.common.TimestampedEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.UUID

/**
 * scenarios (발표용 시나리오). docs/db-design.md 5.10 참조.
 * [dataset]은 시연 데이터 생성 규칙을 설명하는 JSONB 요약이며, 실제 생성 로직은
 * [com.bayerna.reserveforces.application.ScenarioService]의 코드에서 담당한다.
 */
@Entity
@Table(name = "scenarios")
class Scenario(
    @Column(name = "code", nullable = false, unique = true, length = 50)
    var code: String,

    @Column(name = "name", nullable = false, length = 100)
    var name: String,

    @Column(name = "description", columnDefinition = "text")
    var description: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dataset", nullable = false, columnDefinition = "jsonb")
    var dataset: String,

    @Column(name = "enabled", nullable = false)
    var enabled: Boolean = true,

    id: UUID = UUID.randomUUID(),
) : TimestampedEntity(id)
