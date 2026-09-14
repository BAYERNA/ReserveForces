package com.bayerna.reserveforces.application

import com.bayerna.reserveforces.common.ApiException
import com.bayerna.reserveforces.domain.mobilization.Mobilization
import com.bayerna.reserveforces.domain.mobilization.MobilizationStatus
import com.bayerna.reserveforces.domain.scenario.Scenario
import com.bayerna.reserveforces.domain.scenario.ScenarioRun
import com.bayerna.reserveforces.domain.scenario.ScenarioRunStatus
import com.bayerna.reserveforces.domain.target.MobilizationTarget
import com.bayerna.reserveforces.domain.user.UserAccount
import com.bayerna.reserveforces.repository.AttendanceRepository
import com.bayerna.reserveforces.repository.LocationRepository
import com.bayerna.reserveforces.repository.MilitaryUnitRepository
import com.bayerna.reserveforces.repository.MobilizationRepository
import com.bayerna.reserveforces.repository.MobilizationTargetRepository
import com.bayerna.reserveforces.repository.ReservistRepository
import com.bayerna.reserveforces.repository.RuleEvaluationRepository
import com.bayerna.reserveforces.repository.ScenarioRepository
import com.bayerna.reserveforces.repository.ScenarioRunRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.OffsetDateTime
import kotlin.random.Random

/**
 * FR-SCN-001~003: 발표용 시나리오 선택·재생·초기화. 외부 네트워크 없이 로컬 DB만으로 동작하며,
 * 시나리오를 재실행할 때마다 동일한 분포(고정 시드 난수)로 재현 가능한 데이터를 생성한다 (NFR-REL-001).
 */
@Service
class ScenarioService(
    private val scenarioRepository: ScenarioRepository,
    private val scenarioRunRepository: ScenarioRunRepository,
    private val mobilizationRepository: MobilizationRepository,
    private val mobilizationTargetRepository: MobilizationTargetRepository,
    private val attendanceRepository: AttendanceRepository,
    private val ruleEvaluationRepository: RuleEvaluationRepository,
    private val reservistRepository: ReservistRepository,
    private val unitRepository: MilitaryUnitRepository,
    private val locationRepository: LocationRepository,
    private val attendanceService: AttendanceService,
    private val auditLogService: AuditLogService,
) {

    companion object {
        private val GUARANTEED_DEMO_IDENTIFIERS = setOf("RSV-0001", "RSV-0002", "RSV-0003")
    }

    fun listScenarios(): List<Scenario> = scenarioRepository.findAll().filter { it.enabled }

    @Transactional
    fun run(scenarioCode: String, actor: UserAccount?): Mobilization {
        val scenario = scenarioRepository.findByCode(scenarioCode)
            .orElseThrow { ApiException.notFound("시나리오를 찾을 수 없습니다: $scenarioCode") }

        val reservists = reservistRepository.findAll()
        val targetCount = targetCountFor(scenario.code)
        if (reservists.size < targetCount) {
            throw ApiException.badRequest("시나리오 실행에 필요한 예비군 시드 데이터가 부족합니다.")
        }

        val unit = unitRepository.findAll().firstOrNull()
            ?: throw ApiException.badRequest("소집부대 마스터 데이터가 없습니다.")
        val location = locationRepository.findAll().firstOrNull()
            ?: throw ApiException.badRequest("소집 장소 마스터 데이터가 없습니다.")

        val scheduledStart = OffsetDateTime.now().withHour(9).withMinute(0).withSecond(0).withNano(0)
        val mobilization = mobilizationRepository.save(
            Mobilization(
                unit = unit,
                location = location,
                name = "${scenario.name} 시연 (${scheduledStart.toLocalDate()})",
                scheduledStartAt = scheduledStart,
                status = MobilizationStatus.IN_PROGRESS,
            ),
        )

        val scenarioRun = scenarioRunRepository.save(
            ScenarioRun(
                scenario = scenario,
                mobilization = mobilization,
                runStatus = ScenarioRunStatus.RUNNING,
                executedBy = actor,
            ),
        )

        val random = Random(scenario.code.hashCode())
        // README에 안내된 데모 로그인 계정(reservist01~03)은 항상 시나리오에 포함시켜,
        // 어떤 시나리오를 실행하더라도 예비군 화면 시연이 막히지 않도록 한다.
        val guaranteed = reservists.filter { it.demoIdentifier in GUARANTEED_DEMO_IDENTIFIERS }
        val remaining = reservists.filterNot { it.demoIdentifier in GUARANTEED_DEMO_IDENTIFIERS }
            .shuffled(Random(scenario.code.hashCode() * 31))
        val pool = (guaranteed + remaining).take(targetCount)

        pool.forEachIndexed { index, reservist ->
            val target = mobilizationTargetRepository.save(
                MobilizationTarget(mobilization = mobilization, reservist = reservist),
            )
            applyCase(scenario.code, target, index, random, actor)
        }

        scenarioRun.runStatus = ScenarioRunStatus.COMPLETED
        scenarioRun.completedAt = OffsetDateTime.now()
        scenarioRunRepository.save(scenarioRun)

        auditLogService.record(
            actor = actor,
            action = "SCENARIO_RUN",
            targetType = "mobilization",
            targetId = mobilization.id,
            after = mapOf("scenarioCode" to scenario.code, "targetCount" to targetCount),
        )

        return mobilization
    }

    /** FR-SCN-003: 시연 종료 후 소집/대상/입영/판정 데이터를 초기화한다 (마스터 데이터는 유지). */
    @Transactional
    fun reset(actor: UserAccount?) {
        ruleEvaluationRepository.deleteAll()
        attendanceRepository.deleteAll()
        scenarioRunRepository.deleteAll()
        mobilizationTargetRepository.deleteAll()
        mobilizationRepository.deleteAll()

        auditLogService.record(
            actor = actor,
            action = "DEMO_RESET",
            targetType = "mobilization",
            targetId = null,
        )
    }

    private fun targetCountFor(code: String): Int = when (code) {
        "SCN-LARGE" -> 100
        else -> 20
    }

    /**
     * 시나리오별 케이스 분포를 적용한다. AttendanceService의 정식 판정 파이프라인을 그대로 재사용해
     * 실제 관리자 조작과 동일한 경로로 Rule Engine이 실행되도록 한다.
     */
    private fun applyCase(scenarioCode: String, target: MobilizationTarget, index: Int, random: Random, actor: UserAccount?) {
        val scheduled = target.mobilization.scheduledStartAt

        when (scenarioCode) {
            "SCN-NORMAL" -> {
                val delay = random.nextLong(-15, 20)
                attendanceService.recordAttendance(target.id, scheduled.plusMinutes(delay), randomDistance(random, 0.0, 95.0), actor)
            }

            "SCN-DELAY" -> {
                val delay = when (index % 3) {
                    0 -> random.nextLong(-10, 30)
                    1 -> random.nextLong(20, 60)
                    else -> random.nextLong(61, 150)
                }
                attendanceService.recordAttendance(target.id, scheduled.plusMinutes(delay), randomDistance(random, 0.0, 220.0), actor)
            }

            "SCN-EXCEPTION" -> {
                if (index % 3 == 0) {
                    attendanceService.recordAttendance(target.id, null, null, actor)
                    attendanceService.markException(target.id, "입영 데이터 누락 - 현장 확인 필요", actor)
                } else {
                    val delay = random.nextLong(-10, 70)
                    attendanceService.recordAttendance(target.id, scheduled.plusMinutes(delay), randomDistance(random, 0.0, 150.0), actor)
                }
            }

            "SCN-LARGE" -> {
                when {
                    index % 20 == 0 -> attendanceService.markAbsent(target.id, actor)
                    index % 15 == 0 -> {
                        attendanceService.recordAttendance(target.id, null, null, actor)
                        attendanceService.markException(target.id, "대규모 입영 중 신원 확인 지연", actor)
                    }
                    else -> {
                        val delay = if (index % 4 == 0) random.nextLong(20, 130) else random.nextLong(-15, 20)
                        attendanceService.recordAttendance(target.id, scheduled.plusMinutes(delay), randomDistance(random, 0.0, 230.0), actor)
                    }
                }
            }

            else -> attendanceService.recordAttendance(target.id, scheduled, randomDistance(random, 0.0, 50.0), actor)
        }
    }

    private fun randomDistance(random: Random, min: Double, max: Double): BigDecimal =
        BigDecimal.valueOf(min + random.nextDouble() * (max - min)).setScale(2, java.math.RoundingMode.HALF_UP)
}
