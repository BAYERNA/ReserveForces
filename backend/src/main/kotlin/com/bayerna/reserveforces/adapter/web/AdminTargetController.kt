package com.bayerna.reserveforces.adapter.web

import com.bayerna.reserveforces.adapter.web.dto.MarkExceptionRequest
import com.bayerna.reserveforces.adapter.web.dto.RecordAttendanceRequest
import com.bayerna.reserveforces.adapter.web.dto.TargetResponse
import com.bayerna.reserveforces.application.AttendanceService
import com.bayerna.reserveforces.application.TargetService
import com.bayerna.reserveforces.config.AuthenticatedUser
import com.bayerna.reserveforces.domain.target.TargetStatus
import com.bayerna.reserveforces.domain.user.UserAccount
import com.bayerna.reserveforces.repository.UserAccountRepository
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/** FR-DASH-003/004(대상자 검색/상세), FR-MOB-003(입영 상태 변경), FR-RULE-002~004(자동판정). */
@RestController
@RequestMapping("/api/v1/admin")
class AdminTargetController(
    private val targetService: TargetService,
    private val attendanceService: AttendanceService,
    private val userAccountRepository: UserAccountRepository,
) {

    @GetMapping("/targets")
    fun listTargets(
        @RequestParam(required = false) mobilizationId: UUID?,
        @RequestParam(required = false) status: TargetStatus?,
        @RequestParam(required = false) query: String?,
    ): List<TargetResponse> =
        targetService.list(mobilizationId, status, query).map { target ->
            TargetResponse.from(target, targetService.findAttendance(target.id).orElse(null))
        }

    @GetMapping("/targets/{id}")
    fun getTarget(@PathVariable id: UUID): TargetResponse {
        val target = targetService.getDetail(id)
        return TargetResponse.from(target, targetService.findAttendance(id).orElse(null))
    }

    @PostMapping("/attendance/{id}")
    fun recordAttendance(
        @PathVariable id: UUID,
        @RequestBody request: RecordAttendanceRequest,
        @AuthenticationPrincipal principal: AuthenticatedUser,
    ): TargetResponse {
        val attendance = attendanceService.recordAttendance(id, request.arrivedAt, request.distanceKm, resolveActor(principal))
        return TargetResponse.from(attendance.target, attendance)
    }

    @PostMapping("/targets/{id}/absent")
    fun markAbsent(@PathVariable id: UUID, @AuthenticationPrincipal principal: AuthenticatedUser): TargetResponse {
        val target = attendanceService.markAbsent(id, resolveActor(principal))
        return TargetResponse.from(target, targetService.findAttendance(id).orElse(null))
    }

    @PostMapping("/targets/{id}/exception")
    fun markException(
        @PathVariable id: UUID,
        @Valid @RequestBody request: MarkExceptionRequest,
        @AuthenticationPrincipal principal: AuthenticatedUser,
    ): TargetResponse {
        val target = attendanceService.markException(id, request.reason, resolveActor(principal))
        return TargetResponse.from(target, targetService.findAttendance(id).orElse(null))
    }

    @PostMapping("/targets/{id}/complete")
    fun completeTarget(@PathVariable id: UUID, @AuthenticationPrincipal principal: AuthenticatedUser): TargetResponse {
        val target = attendanceService.completeTarget(id, resolveActor(principal))
        return TargetResponse.from(target, targetService.findAttendance(id).orElse(null))
    }

    @PostMapping("/rules/evaluate/{targetId}")
    fun evaluate(@PathVariable targetId: UUID, @AuthenticationPrincipal principal: AuthenticatedUser): TargetResponse {
        val attendance = attendanceService.reevaluate(targetId, resolveActor(principal))
        return TargetResponse.from(attendance.target, attendance)
    }

    private fun resolveActor(principal: AuthenticatedUser): UserAccount? =
        userAccountRepository.findById(principal.userId).orElse(null)
}
