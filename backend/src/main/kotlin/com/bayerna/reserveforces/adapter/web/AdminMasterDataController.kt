package com.bayerna.reserveforces.adapter.web

import com.bayerna.reserveforces.adapter.web.dto.CreateMobilizationRequest
import com.bayerna.reserveforces.adapter.web.dto.LocationResponse
import com.bayerna.reserveforces.adapter.web.dto.MobilizationResponse
import com.bayerna.reserveforces.adapter.web.dto.UnitResponse
import com.bayerna.reserveforces.common.ApiException
import com.bayerna.reserveforces.domain.mobilization.Mobilization
import com.bayerna.reserveforces.repository.LocationRepository
import com.bayerna.reserveforces.repository.MobilizationRepository
import com.bayerna.reserveforces.repository.MilitaryUnitRepository
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/** FR-MOB-001: 소집 회차 등록·조회. 부대/장소는 화면 필터·표시를 위한 보조 조회. */
@RestController
@RequestMapping("/api/v1/admin")
class AdminMasterDataController(
    private val mobilizationRepository: MobilizationRepository,
    private val unitRepository: MilitaryUnitRepository,
    private val locationRepository: LocationRepository,
) {

    @GetMapping("/mobilizations")
    fun listMobilizations(): List<MobilizationResponse> =
        mobilizationRepository.findAll().sortedByDescending { it.scheduledStartAt }.map(MobilizationResponse::from)

    @PostMapping("/mobilizations")
    fun createMobilization(@Valid @RequestBody request: CreateMobilizationRequest): MobilizationResponse {
        val unit = unitRepository.findById(request.unitId!!)
            .orElseThrow { ApiException.notFound("소집부대를 찾을 수 없습니다: ${request.unitId}") }
        val location = locationRepository.findById(request.locationId!!)
            .orElseThrow { ApiException.notFound("소집 장소를 찾을 수 없습니다: ${request.locationId}") }

        val mobilization = mobilizationRepository.save(
            Mobilization(
                unit = unit,
                location = location,
                name = request.name,
                scheduledStartAt = request.scheduledStartAt!!,
                scheduledEndAt = request.scheduledEndAt,
            ),
        )
        return MobilizationResponse.from(mobilization)
    }

    @GetMapping("/units")
    fun listUnits(): List<UnitResponse> = unitRepository.findAll().map(UnitResponse::from)

    @GetMapping("/locations")
    fun listLocations(): List<LocationResponse> = locationRepository.findAll().map(LocationResponse::from)
}
