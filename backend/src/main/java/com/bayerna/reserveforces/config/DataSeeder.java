package com.bayerna.reserveforces.config;

import com.bayerna.reserveforces.application.JudgmentService;
import com.bayerna.reserveforces.domain.notice.CallupNotice;
import com.bayerna.reserveforces.domain.notice.NoticeStatus;
import com.bayerna.reserveforces.domain.reservist.Reservist;
import com.bayerna.reserveforces.domain.reservist.ReservistStatus;
import com.bayerna.reserveforces.domain.unit.Unit;
import com.bayerna.reserveforces.domain.user.Role;
import com.bayerna.reserveforces.domain.user.UserAccount;
import com.bayerna.reserveforces.repository.CallupNoticeRepository;
import com.bayerna.reserveforces.repository.ReservistRepository;
import com.bayerna.reserveforces.repository.UnitRepository;
import com.bayerna.reserveforces.repository.UserAccountRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * FR-11: 네트워크 연결 없이도 전체 기능을 시연할 수 있도록, 앱 구동 시 자체 시드 데이터를
 * 적재하고 판정 룰엔진(FR-04, FR-05)을 실행하여 판정결과까지 미리 산출해 둔다.
 * (docs/db-design.md 6절 시드 데이터 및 시연 전략 참조)
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String DEMO_PASSWORD = "reserve1234!";

    private final UnitRepository unitRepository;
    private final ReservistRepository reservistRepository;
    private final UserAccountRepository userAccountRepository;
    private final CallupNoticeRepository callupNoticeRepository;
    private final JudgmentService judgmentService;
    private final PasswordEncoder passwordEncoder;
    private final SeedProperties seedProperties;

    public DataSeeder(
            UnitRepository unitRepository,
            ReservistRepository reservistRepository,
            UserAccountRepository userAccountRepository,
            CallupNoticeRepository callupNoticeRepository,
            JudgmentService judgmentService,
            PasswordEncoder passwordEncoder,
            SeedProperties seedProperties) {
        this.unitRepository = unitRepository;
        this.reservistRepository = reservistRepository;
        this.userAccountRepository = userAccountRepository;
        this.callupNoticeRepository = callupNoticeRepository;
        this.judgmentService = judgmentService;
        this.passwordEncoder = passwordEncoder;
        this.seedProperties = seedProperties;
    }

    private static final String[] SURNAMES = {"김", "이", "박", "최", "정", "강", "조", "윤", "장", "임"};
    private static final String[] GIVEN_NAMES = {
        "민준", "서준", "도윤", "예준", "시우", "하준", "주원", "지호", "지훈", "준서",
        "건우", "현우", "우진", "선우", "연우", "정우", "승우", "준혁", "은우", "이안",
        "지우", "유준", "동현", "재원"
    };

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedProperties.enabled()) {
            log.info("시드 데이터 적재가 비활성화되어 있습니다 (app.seed.enabled=false).");
            return;
        }
        if (unitRepository.count() > 0) {
            log.info("이미 데이터가 존재하여 시드 적재를 건너뜁니다.");
            return;
        }

        log.info("시연용 시나리오 데이터 적재를 시작합니다...");

        List<Unit> units = seedUnits();
        List<Reservist> reservists = seedReservistsWithScenarios(units);
        seedDemoAccounts(units, reservists);

        log.info("시드 데이터 적재 완료: 부대 {}개, 소집대상자 {}명", units.size(), reservists.size());
    }

    private List<Unit> seedUnits() {
        List<Unit> units = List.of(
                Unit.builder()
                        .unitName("제1보충대대")
                        .location("경기도 양주시 회암로 123")
                        .entryDeadlineTime(LocalTime.of(9, 0))
                        .contact("031-1234-5678")
                        .build(),
                Unit.builder()
                        .unitName("제2동원훈련단")
                        .location("강원도 원주시 치악로 45")
                        .entryDeadlineTime(LocalTime.of(8, 30))
                        .contact("033-2345-6789")
                        .build(),
                Unit.builder()
                        .unitName("제3향토사단 동원훈련장")
                        .location("충청남도 논산시 훈련로 67")
                        .entryDeadlineTime(LocalTime.of(9, 30))
                        .contact("041-3456-7890")
                        .build());
        return unitRepository.saveAll(units);
    }

    /**
     * 지연입소(허용/불허)·조기퇴소(허용/불허)·미입영 케이스가 고르게 나타나도록
     * 24명의 소집대상자를 시드로 등록하고, 룰엔진으로 판정결과를 자동 산출한다.
     */
    private List<Reservist> seedReservistsWithScenarios(List<Unit> units) {
        LocalDate callupDate = LocalDate.now();
        int total = GIVEN_NAMES.length;

        for (int i = 0; i < total; i++) {
            Unit unit = units.get(i % units.size());
            String name = SURNAMES[i % SURNAMES.length] + GIVEN_NAMES[i];
            BigDecimal distanceKm = BigDecimal.valueOf((i * 17 % 220) + 8).setScale(1);

            Reservist reservist = Reservist.builder()
                    .displayName(name)
                    .residenceDistanceKm(distanceKm)
                    .unit(unit)
                    .status(ReservistStatus.BEFORE_CALLUP)
                    .build();
            reservist = reservistRepository.save(reservist);

            LocalDateTime scheduled = LocalDateTime.of(callupDate, unit.getEntryDeadlineTime());
            callupNoticeRepository.save(CallupNotice.builder()
                    .reservist(reservist)
                    .unit(unit)
                    .scheduledDatetime(scheduled)
                    .noticeStatus(NoticeStatus.SENT)
                    .build());

            LocalDateTime actualEntry = buildScenarioEntryDatetime(scheduled, i);
            judgmentService.recordEntryAndJudgeLateEntry(reservist.getReservistId(), actualEntry);
            judgmentService.createEarlyDischargeJudgment(reservist);
        }

        return reservistRepository.findAll();
    }

    /**
     * 4가지 시나리오(정시/1시간 이내 지연/1시간 초과 지연/미입영)를 순환시켜 생성한다.
     */
    private LocalDateTime buildScenarioEntryDatetime(LocalDateTime deadline, int index) {
        int scenario = index % 4;
        return switch (scenario) {
            case 0 -> deadline.minusMinutes(5 + (index % 10));   // 정시 입영
            case 1 -> deadline.plusMinutes(15 + (index % 40));   // 1시간 이내 지연 (허용)
            case 2 -> deadline.plusMinutes(65 + (index % 50));   // 1시간 초과 지연 (불허)
            default -> null;                                     // 미입영
        };
    }

    private void seedDemoAccounts(List<Unit> units, List<Reservist> reservists) {
        String hash = passwordEncoder.encode(DEMO_PASSWORD);

        userAccountRepository.save(UserAccount.builder()
                .loginId("admin")
                .passwordHash(hash)
                .role(Role.ADMIN)
                .unit(units.get(0))
                .build());

        for (int i = 0; i < units.size() && i < reservists.size(); i++) {
            Reservist demoReservist = reservists.get(i * (reservists.size() / units.size()));
            userAccountRepository.save(UserAccount.builder()
                    .loginId("reservist0" + (i + 1))
                    .passwordHash(hash)
                    .role(Role.RESERVIST)
                    .reservist(demoReservist)
                    .build());
        }

        log.info("데모 계정 생성 완료 (공통 비밀번호: {}) — admin / reservist01 / reservist02 / reservist03", DEMO_PASSWORD);
    }
}
