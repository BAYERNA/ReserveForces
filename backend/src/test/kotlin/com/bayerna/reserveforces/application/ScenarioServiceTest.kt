package com.bayerna.reserveforces.application

import com.bayerna.reserveforces.common.ApiException
import com.bayerna.reserveforces.domain.location.Location
import com.bayerna.reserveforces.domain.location.LocationType
import com.bayerna.reserveforces.domain.reservist.Reservist
import com.bayerna.reserveforces.domain.scenario.Scenario
import com.bayerna.reserveforces.domain.target.MobilizationTarget
import com.bayerna.reserveforces.domain.unit.MilitaryUnit
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
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.Optional
import kotlin.test.assertTrue

/**
 * FR-SCN-001~002: 어떤 시나리오를 실행하더라도 데모 로그인 계정(reservist01~03)이
 * 항상 소집 대상에 포함되는지 검증한다 (README 명세, 예비군 화면 시연이 막히지 않아야 함).
 */
class ScenarioServiceTest {

    private lateinit var scenarioRepository: ScenarioRepository
    private lateinit var reservistRepository: ReservistRepository
    private lateinit var unitRepository: MilitaryUnitRepository
    private lateinit var locationRepository: LocationRepository
    private lateinit var mobilizationRepository: MobilizationRepository
    private lateinit var mobilizationTargetRepository: MobilizationTargetRepository
    private lateinit var scenarioRunRepository: ScenarioRunRepository
    private lateinit var scenarioService: ScenarioService

    private fun <T> any(): T {
        Mockito.any<T>()
        @Suppress("UNCHECKED_CAST")
        return null as T
    }

    @BeforeEach
    fun setUp() {
        scenarioRepository = mock(ScenarioRepository::class.java)
        reservistRepository = mock(ReservistRepository::class.java)
        unitRepository = mock(MilitaryUnitRepository::class.java)
        locationRepository = mock(LocationRepository::class.java)
        mobilizationRepository = mock(MobilizationRepository::class.java)
        mobilizationTargetRepository = mock(MobilizationTargetRepository::class.java)
        scenarioRunRepository = mock(ScenarioRunRepository::class.java)

        scenarioService = ScenarioService(
            scenarioRepository = scenarioRepository,
            scenarioRunRepository = scenarioRunRepository,
            mobilizationRepository = mobilizationRepository,
            mobilizationTargetRepository = mobilizationTargetRepository,
            attendanceRepository = mock(AttendanceRepository::class.java),
            ruleEvaluationRepository = mock(RuleEvaluationRepository::class.java),
            reservistRepository = reservistRepository,
            unitRepository = unitRepository,
            locationRepository = locationRepository,
            attendanceService = mock(AttendanceService::class.java),
            auditLogService = mock(AuditLogService::class.java),
        )

        `when`(unitRepository.findAll()).thenReturn(listOf(MilitaryUnit(code = "U1", name = "제1보충대대")))
        `when`(locationRepository.findAll()).thenReturn(listOf(Location(name = "입영장", locationType = LocationType.MOBILIZATION_SITE)))
        `when`(mobilizationRepository.save(any())).thenAnswer { it.arguments[0] }
        `when`(mobilizationTargetRepository.save(any())).thenAnswer { it.arguments[0] as MobilizationTarget }
        `when`(scenarioRunRepository.save(any())).thenAnswer { it.arguments[0] }
    }

    private fun reservistWith(demoIdentifier: String): Reservist {
        val user = mock(UserAccount::class.java)
        return Reservist(user = user, demoIdentifier = demoIdentifier, name = "예비군-$demoIdentifier")
    }

    @Test
    fun `대규모 예비군 풀에서도 데모 로그인 계정 3명은 항상 소집 대상에 포함된다`() {
        val scenario = Scenario(code = "SCN-NORMAL", name = "정상 입영 시연", dataset = "{}")
        `when`(scenarioRepository.findByCode("SCN-NORMAL")).thenReturn(Optional.of(scenario))

        val guaranteed = listOf("RSV-0001", "RSV-0002", "RSV-0003").map(::reservistWith)
        val others = (1..100).map { reservistWith("RSV-OTHER-$it") }
        `when`(reservistRepository.findAll()).thenReturn(guaranteed + others)

        scenarioService.run("SCN-NORMAL", null)

        val captor = org.mockito.ArgumentCaptor.forClass(MobilizationTarget::class.java)
        Mockito.verify(mobilizationTargetRepository, Mockito.atLeastOnce()).save(captor.capture())
        val savedTargets = captor.allValues

        val savedIdentifiers = savedTargets.map { it.reservist.demoIdentifier }.toSet()
        assertTrue(savedIdentifiers.containsAll(setOf("RSV-0001", "RSV-0002", "RSV-0003")))
        assertTrue(savedTargets.size == 20)
    }

    @Test
    fun `예비군 시드 데이터가 목표 인원보다 적으면 예외를 발생시킨다`() {
        val scenario = Scenario(code = "SCN-LARGE", name = "대규모 입영 시연", dataset = "{}")
        `when`(scenarioRepository.findByCode("SCN-LARGE")).thenReturn(Optional.of(scenario))
        `when`(reservistRepository.findAll()).thenReturn(listOf(reservistWith("RSV-0001")))

        assertThrows(ApiException::class.java) {
            scenarioService.run("SCN-LARGE", null)
        }
    }
}
