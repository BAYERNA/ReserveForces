package com.bayerna.reserveforces.adapter.web.dto

import com.bayerna.reserveforces.domain.location.Location
import com.bayerna.reserveforces.domain.mobilization.Mobilization
import com.bayerna.reserveforces.domain.mobilization.MobilizationStatus
import com.bayerna.reserveforces.domain.unit.MilitaryUnit
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.OffsetDateTime
import java.util.UUID

data class UnitResponse(val unitId: UUID, val code: String, val name: String, val regionCode: String?) {
    companion object {
        fun from(unit: MilitaryUnit) = UnitResponse(unit.id, unit.code, unit.name, unit.regionCode)
    }
}

data class LocationResponse(
    val locationId: UUID,
    val name: String,
    val address: String?,
    val locationType: String,
) {
    companion object {
        fun from(location: Location) =
            LocationResponse(location.id, location.name, location.address, location.locationType.name)
    }
}

data class MobilizationResponse(
    val mobilizationId: UUID,
    val name: String,
    val unitName: String,
    val locationName: String,
    val scheduledStartAt: OffsetDateTime,
    val status: MobilizationStatus,
) {
    companion object {
        fun from(mobilization: Mobilization) = MobilizationResponse(
            mobilizationId = mobilization.id,
            name = mobilization.name,
            unitName = mobilization.unit.name,
            locationName = mobilization.location.name,
            scheduledStartAt = mobilization.scheduledStartAt,
            status = mobilization.status,
        )
    }
}

/** FR-MOB-001: 관리자가 소집 회차를 등록한다. */
data class CreateMobilizationRequest(
    @field:NotNull(message = "소집부대를 선택해 주세요.") val unitId: UUID?,
    @field:NotNull(message = "소집 장소를 선택해 주세요.") val locationId: UUID?,
    @field:NotBlank(message = "소집 회차명을 입력해 주세요.") val name: String,
    @field:NotNull(message = "소집 예정 일시를 입력해 주세요.") val scheduledStartAt: OffsetDateTime?,
    val scheduledEndAt: OffsetDateTime? = null,
)
