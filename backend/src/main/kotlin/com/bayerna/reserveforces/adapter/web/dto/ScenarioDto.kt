package com.bayerna.reserveforces.adapter.web.dto

import com.bayerna.reserveforces.domain.scenario.Scenario
import java.util.UUID

data class ScenarioResponse(
    val scenarioId: UUID,
    val code: String,
    val name: String,
    val description: String?,
) {
    companion object {
        fun from(scenario: Scenario) = ScenarioResponse(scenario.id, scenario.code, scenario.name, scenario.description)
    }
}

data class ScenarioRunResponse(
    val mobilizationId: UUID,
    val mobilizationName: String,
    val targetCount: Int,
)
