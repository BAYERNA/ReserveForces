package com.bayerna.reserveforces.application

import com.bayerna.reserveforces.domain.audit.AuditLog
import com.bayerna.reserveforces.domain.user.UserAccount
import com.bayerna.reserveforces.repository.AuditLogRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import java.util.UUID

/** FR-AUD-001: 주요 상태 변경을 시간순으로 기록한다. */
@Service
class AuditLogService(
    private val auditLogRepository: AuditLogRepository,
    private val objectMapper: ObjectMapper,
) {

    fun record(
        actor: UserAccount?,
        action: String,
        targetType: String,
        targetId: UUID?,
        before: Any? = null,
        after: Any? = null,
    ) {
        auditLogRepository.save(
            AuditLog(
                actor = actor,
                action = action,
                targetType = targetType,
                targetId = targetId,
                beforeData = before?.let { objectMapper.writeValueAsString(it) },
                afterData = after?.let { objectMapper.writeValueAsString(it) },
            ),
        )
    }

    fun listAll() = auditLogRepository.findAllByOrderByCreatedAtDesc()
}
