package com.bayerna.reserveforces.config

import com.bayerna.reserveforces.application.ScenarioService
import com.bayerna.reserveforces.common.ActiveStatus
import com.bayerna.reserveforces.domain.location.Location
import com.bayerna.reserveforces.domain.location.LocationType
import com.bayerna.reserveforces.domain.reservist.Reservist
import com.bayerna.reserveforces.domain.rule.Rule
import com.bayerna.reserveforces.domain.rule.RuleConditionSpec
import com.bayerna.reserveforces.domain.rule.RuleOperator
import com.bayerna.reserveforces.domain.rule.RuleType
import com.bayerna.reserveforces.domain.scenario.Scenario
import com.bayerna.reserveforces.domain.unit.MilitaryUnit
import com.bayerna.reserveforces.domain.user.UserAccount
import com.bayerna.reserveforces.domain.user.UserRole
import com.bayerna.reserveforces.repository.LocationRepository
import com.bayerna.reserveforces.repository.MilitaryUnitRepository
import com.bayerna.reserveforces.repository.ReservistRepository
import com.bayerna.reserveforces.repository.RuleRepository
import com.bayerna.reserveforces.repository.ScenarioRepository
import com.bayerna.reserveforces.repository.UserAccountRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

/**
 * FR-SCN-001, NFR-OFF-001: 네트워크 연결 없이도 전체 기능을 시연할 수 있도록, 앱 구동 시
 * 마스터 데이터(부대·장소·규칙·시나리오·예비군 풀·계정)를 적재하고 기본 시나리오를 한 번 실행해 둔다.
 */
@Component
class DataSeeder(
    private val unitRepository: MilitaryUnitRepository,
    private val locationRepository: LocationRepository,
    private val ruleRepository: RuleRepository,
    private val scenarioRepository: ScenarioRepository,
    private val reservistRepository: ReservistRepository,
    private val userAccountRepository: UserAccountRepository,
    private val passwordEncoder: PasswordEncoder,
    private val objectMapper: ObjectMapper,
    private val scenarioService: ScenarioService,
    private val seedProperties: SeedProperties,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(DataSeeder::class.java)

    companion object {
        const val DEMO_PASSWORD = "reserve1234!"
        const val RESERVIST_POOL_SIZE = 100
    }

    private val surnames = listOf("김", "이", "박", "최", "정", "강", "조", "윤", "장", "임")
    private val givenNames = listOf(
        "민준", "서준", "도윤", "예준", "시우", "하준", "주원", "지호", "지훈", "준서",
        "건우", "현우", "우진", "선우", "연우", "정우", "승우", "준혁", "은우", "이안",
    )
    private val regions = listOf(
        "서울특별시 강남구", "경기도 수원시", "인천광역시 남동구", "강원특별자치도 원주시",
        "충청남도 논산시", "대전광역시 유성구", "전라북도 전주시", "광주광역시 서구",
        "경상북도 포항시", "부산광역시 해운대구",
    )

    @Transactional
    override fun run(args: ApplicationArguments) {
        if (!seedProperties.enabled) {
            log.info("시드 데이터 적재가 비활성화되어 있습니다 (app.seed.enabled=false).")
            return
        }
        if (unitRepository.count() > 0) {
            log.info("이미 데이터가 존재하여 시드 적재를 건너뜁니다.")
            return
        }

        log.info("마스터 데이터 적재를 시작합니다...")

        seedUnitsAndLocations()
        seedRules()
        seedScenarios()
        val reservists = seedReservists()
        seedAccounts()

        log.info("마스터 데이터 적재 완료: 예비군 풀 {}명", reservists.size)

        val admin = userAccountRepository.findByLoginId("admin").orElseThrow()
        scenarioService.run("SCN-DELAY", admin)
        log.info("초기 시연 시나리오(SCN-DELAY) 실행 완료. 데모 계정 공통 비밀번호: {}", DEMO_PASSWORD)
    }

    private fun seedUnitsAndLocations() {
        val units = listOf(
            MilitaryUnit(code = "UNIT-01", name = "제1보충대대", regionCode = "GG"),
            MilitaryUnit(code = "UNIT-02", name = "제2동원훈련단", regionCode = "GW"),
            MilitaryUnit(code = "UNIT-03", name = "제3향토사단 동원훈련장", regionCode = "CN"),
        )
        unitRepository.saveAll(units)

        val locations = listOf(
            Location(
                name = "제1보충대대 입영장",
                address = "경기도 양주시 회암로 123",
                latitude = BigDecimal("37.7853"),
                longitude = BigDecimal("127.0455"),
                locationType = LocationType.MOBILIZATION_SITE,
            ),
            Location(
                name = "제2동원훈련단 훈련장",
                address = "강원특별자치도 원주시 치악로 45",
                latitude = BigDecimal("37.3422"),
                longitude = BigDecimal("127.9202"),
                locationType = LocationType.TRAINING_SITE,
            ),
            Location(
                name = "제3향토사단 동원훈련장",
                address = "충청남도 논산시 훈련로 67",
                latitude = BigDecimal("36.1872"),
                longitude = BigDecimal("127.0989"),
                locationType = LocationType.TRAINING_SITE,
            ),
        )
        locationRepository.saveAll(locations)
    }

    /**
     * FR-RULE-002/003: 지연입소(1시간 이내 허용)·조기퇴소(100km 이상) 판정 기준을 코드가 아닌
     * 데이터로 등록한다. 공모전 기획 단계의 예시 수치이며, 실제 서비스 전환 시 공식 규정으로 교체한다.
     */
    private fun seedRules() {
        fun condition(field: String, operator: RuleOperator, value: Double, unit: String) =
            objectMapper.writeValueAsString(RuleConditionSpec(field, operator, value, unit))

        val rules = listOf(
            Rule(
                code = "LATE_ENTRY_WITHIN_GRACE",
                name = "지연입소 허용(1시간 이내)",
                version = "v1",
                description = "예정 입영시각 대비 60분 이내 도착은 정상(NORMAL)으로 판정한다.",
                ruleType = RuleType.TIME,
                conditions = condition("arrival_delay_minutes", RuleOperator.LTE, 60.0, "MINUTE"),
                resultCode = "NORMAL",
                priority = 10,
            ),
            Rule(
                code = "LATE_ENTRY_EXCEEDS_GRACE",
                name = "지연입소 불허(1시간 초과)",
                version = "v1",
                description = "예정 입영시각 대비 60분을 초과한 도착은 지연(DELAY)으로 판정한다.",
                ruleType = RuleType.TIME,
                conditions = condition("arrival_delay_minutes", RuleOperator.GT, 60.0, "MINUTE"),
                resultCode = "DELAY",
                priority = 20,
            ),
            Rule(
                code = "EARLY_DEPARTURE_ELIGIBLE",
                name = "조기퇴소 허용(100km 이상)",
                version = "v1",
                description = "거주지-소집부대 거리가 100km 이상이면 조기퇴소 대상으로 판정한다.",
                ruleType = RuleType.DISTANCE,
                conditions = condition("distance_km", RuleOperator.GTE, 100.0, "KM"),
                resultCode = "EARLY_DEPARTURE_ELIGIBLE",
                priority = 10,
            ),
            Rule(
                code = "EARLY_DEPARTURE_NOT_ELIGIBLE",
                name = "조기퇴소 비대상(100km 미만)",
                version = "v1",
                description = "거주지-소집부대 거리가 100km 미만이면 조기퇴소 비대상으로 판정한다.",
                ruleType = RuleType.DISTANCE,
                conditions = condition("distance_km", RuleOperator.LT, 100.0, "KM"),
                resultCode = "EARLY_DEPARTURE_NOT_ELIGIBLE",
                priority = 20,
            ),
        )
        ruleRepository.saveAll(rules)
    }

    private fun seedScenarios() {
        fun dataset(targetCount: Int, focus: String) =
            objectMapper.writeValueAsString(mapOf("targetCount" to targetCount, "focus" to focus))

        val scenarios = listOf(
            Scenario(
                code = "SCN-NORMAL",
                name = "정상 입영",
                description = "정상 입영 위주 20명 — 기본 관제 흐름 시연",
                dataset = dataset(20, "NORMAL"),
            ),
            Scenario(
                code = "SCN-DELAY",
                name = "지연 입영",
                description = "지연입소 허용/불허 사례를 포함한 20명 — Rule Engine 자동판정 시연",
                dataset = dataset(20, "DELAY"),
            ),
            Scenario(
                code = "SCN-EXCEPTION",
                name = "예외 상황",
                description = "필수 입력값 누락 등 예외 사례를 포함한 20명 — 담당자 확인 흐름 시연",
                dataset = dataset(20, "EXCEPTION"),
            ),
            Scenario(
                code = "SCN-LARGE",
                name = "대규모 입영",
                description = "100명 대규모 동시 입영 — 관제 대시보드 실용성 시연",
                dataset = dataset(100, "LARGE"),
            ),
        )
        scenarioRepository.saveAll(scenarios)
    }

    /** 지연입소·조기퇴소 각 케이스가 고르게 나타나도록 최대 100명의 예비군 풀을 시드로 등록한다. */
    private fun seedReservists(): List<Reservist> {
        val hash = passwordEncoder.encode(DEMO_PASSWORD)
        val reservists = mutableListOf<Reservist>()

        for (i in 0 until RESERVIST_POOL_SIZE) {
            val loginId = if (i < 3) "reservist0${i + 1}" else "res%04d".format(i + 1)
            val name = surnames[i % surnames.size] + givenNames[i % givenNames.size]

            val user = userAccountRepository.save(
                UserAccount(
                    loginId = loginId,
                    passwordHash = hash,
                    name = name,
                    role = UserRole.RESERVIST,
                    status = ActiveStatus.ACTIVE,
                ),
            )
            val reservist = reservistRepository.save(
                Reservist(
                    user = user,
                    demoIdentifier = "RSV-%04d".format(i + 1),
                    name = name,
                    addressRegion = regions[i % regions.size],
                    status = ActiveStatus.ACTIVE,
                ),
            )
            reservists.add(reservist)
        }
        return reservists
    }

    private fun seedAccounts() {
        val hash = passwordEncoder.encode(DEMO_PASSWORD)

        userAccountRepository.save(
            UserAccount(loginId = "admin", passwordHash = hash, name = "부대 동원 담당자", role = UserRole.ADMIN),
        )
        userAccountRepository.save(
            UserAccount(loginId = "demo", passwordHash = hash, name = "시연 관리자", role = UserRole.DEMO),
        )

        log.info(
            "데모 계정 생성 완료 (공통 비밀번호: {}) — admin(ADMIN) / demo(DEMO) / reservist01~03(RESERVIST)",
            DEMO_PASSWORD,
        )
    }
}
