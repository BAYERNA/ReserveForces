package com.bayerna.reserveforces.adapter.web

import com.bayerna.reserveforces.adapter.web.dto.MyMobilizationResponse
import com.bayerna.reserveforces.adapter.web.dto.MyStatusResponse
import com.bayerna.reserveforces.application.MeService
import com.bayerna.reserveforces.common.ApiException
import com.bayerna.reserveforces.config.AuthenticatedUser
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * 예비군 본인용 API. FR-MOB-004(소집 정보), FR-MOB-005(개인 상태 조회).
 * 항상 JWT 토큰의 reservistId만을 조회 조건으로 사용하여 타인의 정보에 접근할 수 없도록 한다 (NFR-SEC-002).
 */
@RestController
@RequestMapping("/api/v1/me")
class MeController(private val meService: MeService) {

    @GetMapping("/mobilization")
    fun myMobilization(@AuthenticationPrincipal principal: AuthenticatedUser): MyMobilizationResponse {
        val target = meService.getLatestTarget(requireReservistId(principal))
        return MyMobilizationResponse.from(target)
    }

    @GetMapping("/status")
    fun myStatus(@AuthenticationPrincipal principal: AuthenticatedUser): MyStatusResponse {
        val target = meService.getLatestTarget(requireReservistId(principal))
        val attendance = meService.getAttendance(target.id)
        val evaluations = meService.getEvaluations(target.id)
        return MyStatusResponse.from(target, attendance, evaluations)
    }

    private fun requireReservistId(principal: AuthenticatedUser): UUID =
        principal.reservistId ?: throw ApiException.forbidden("예비군 계정에만 허용된 기능입니다.")
}
