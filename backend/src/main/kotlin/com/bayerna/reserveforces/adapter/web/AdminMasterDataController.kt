package com.bayerna.reserveforces.adapter.web

import com.bayerna.reserveforces.adapter.web.dto.LocationResponse
import com.bayerna.reserveforces.adapter.web.dto.MobilizationResponse
import com.bayerna.reserveforces.adapter.web.dto.UnitResponse
import com.bayerna.reserveforces.repository.LocationRepository
import com.bayerna.reserveforces.repository.MobilizationRepository
import com.bayerna.reserveforces.repository.MilitaryUnitRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/** FR-MOB-001: 소집 회차 목록. 부대/장소는 화면 필터·표시를 위한 보조 조회. */
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

    @GetMapping("/units")
    fun listUnits(): List<UnitResponse> = unitRepository.findAll().map(UnitResponse::from)

    @GetMapping("/locations")
    fun listLocations(): List<LocationResponse> = locationRepository.findAll().map(LocationResponse::from)
}
