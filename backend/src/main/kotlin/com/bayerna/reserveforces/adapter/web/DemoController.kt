package com.bayerna.reserveforces.adapter.web

import com.bayerna.reserveforces.adapter.web.dto.ScenarioResponse
import com.bayerna.reserveforces.adapter.web.dto.ScenarioRunResponse
import com.bayerna.reserveforces.application.ScenarioService
import com.bayerna.reserveforces.config.AuthenticatedUser
import com.bayerna.reserveforces.domain.user.UserAccount
import com.bayerna.reserveforces.repository.MobilizationTargetRepository
import com.bayerna.reserveforces.repository.UserAccountRepository
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.HttpStatus

/** FR-SCN-001~003: 발표용 시나리오 선택·재생·초기화 (SCR-07). */
@RestController
@RequestMapping("/api/v1/demo")
class DemoController(
    private val scenarioService: ScenarioService,
    private val mobilizationTargetRepository: MobilizationTargetRepository,
    private val userAccountRepository: UserAccountRepository,
) {

    @GetMapping("/scenarios")
    fun listScenarios(): List<ScenarioResponse> = scenarioService.listScenarios().map(ScenarioResponse::from)

    @PostMapping("/scenarios/{code}/run")
    fun runScenario(@PathVariable code: String, @AuthenticationPrincipal principal: AuthenticatedUser): ScenarioRunResponse {
        val mobilization = scenarioService.run(code, resolveActor(principal))
        val targetCount = mobilizationTargetRepository.findByMobilization_Id(mobilization.id).size
        return ScenarioRunResponse(mobilization.id, mobilization.name, targetCount)
    }

    @PostMapping("/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun reset(@AuthenticationPrincipal principal: AuthenticatedUser) {
        scenarioService.reset(resolveActor(principal))
    }

    private fun resolveActor(principal: AuthenticatedUser): UserAccount? =
        userAccountRepository.findById(principal.userId).orElse(null)
}
