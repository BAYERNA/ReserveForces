package com.bayerna.reserveforces.adapter.web.dto

import com.bayerna.reserveforces.domain.audit.AuditLog
import java.time.OffsetDateTime
import java.util.UUID

data class AuditLogResponse(
    val logId: UUID,
    val actor: String?,
    val action: String,
    val targetType: String,
    val targetId: UUID?,
    val beforeData: String?,
    val afterData: String?,
    val createdAt: OffsetDateTime?,
) {
    companion object {
        fun from(log: AuditLog) = AuditLogResponse(
            logId = log.id,
            actor = log.actor?.name,
            action = log.action,
            targetType = log.targetType,
            targetId = log.targetId,
            beforeData = log.beforeData,
            afterData = log.afterData,
            createdAt = log.createdAt,
        )
    }
}
