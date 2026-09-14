package com.bayerna.reserveforces.adapter.web

import com.bayerna.reserveforces.adapter.web.dto.RuleResponse
import com.bayerna.reserveforces.adapter.web.dto.UpdateRuleRequest
import com.bayerna.reserveforces.application.RuleAdminService
import com.bayerna.reserveforces.config.AuthenticatedUser
import com.bayerna.reserveforces.domain.user.UserAccount
import com.bayerna.reserveforces.repository.UserAccountRepository
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * FR-RULE-006 / NFR-MAINT-001: 규정 변경 시 코드를 수정하지 않고 규칙 데이터(기준값/활성 여부)만
 * 바꿀 수 있도록 하는 관리자 화면용 API. 기준값 변경 시 규칙 버전이 자동으로 올라간다.
 */
@RestController
@RequestMapping("/api/v1/admin/rules")
class AdminRuleController(
    private val ruleAdminService: RuleAdminService,
    private val userAccountRepository: UserAccountRepository,
) {

    @GetMapping
    fun listRules(): List<RuleResponse> = ruleAdminService.listAll().map(RuleResponse::from)

    @PatchMapping("/{id}")
    fun updateRule(
        @PathVariable id: UUID,
        @RequestBody request: UpdateRuleRequest,
        @AuthenticationPrincipal principal: AuthenticatedUser,
    ): RuleResponse {
        val actor: UserAccount? = userAccountRepository.findById(principal.userId).orElse(null)
        return RuleResponse.from(ruleAdminService.updateRule(id, request.value, request.enabled, actor))
    }
}
