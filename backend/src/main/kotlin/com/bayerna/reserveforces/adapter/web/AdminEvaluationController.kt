package com.bayerna.reserveforces.adapter.web

import com.bayerna.reserveforces.adapter.web.dto.AuditLogResponse
import com.bayerna.reserveforces.adapter.web.dto.RuleEvaluationResponse
import com.bayerna.reserveforces.application.AuditLogService
import com.bayerna.reserveforces.application.RuleEvaluationService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/** FR-RULE-005/006, FR-AUD-002: 판정 근거 및 이력 조회 (SCR-08). */
@RestController
@RequestMapping("/api/v1/admin/evaluations")
class AdminEvaluationController(private val ruleEvaluationService: RuleEvaluationService) {

    @GetMapping
    fun listAll(): List<RuleEvaluationResponse> = ruleEvaluationService.listAll().map(RuleEvaluationResponse::from)

    @GetMapping("/{targetId}")
    fun listForTarget(@PathVariable targetId: UUID): List<RuleEvaluationResponse> =
        ruleEvaluationService.listForTarget(targetId).map(RuleEvaluationResponse::from)
}

/** FR-AUD-001: 상태 변경 이력 조회. */
@RestController
@RequestMapping("/api/v1/admin/audit-logs")
class AdminAuditLogController(private val auditLogService: AuditLogService) {

    @GetMapping
    fun listAll(): List<AuditLogResponse> = auditLogService.listAll().map(AuditLogResponse::from)
}
