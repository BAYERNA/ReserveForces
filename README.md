# ReserveForces — 예비군 동원소집 통합 관제 시스템

2026년 「예비전력분야 혁신 아이디어」 공모전(혁신 서비스 개발 분야) 출품작.
부대 담당자용 실시간 관제 대시보드와 예비군용 소집 확인 화면을 하나의 웹 서비스로 연동하고,
지연입소·조기퇴소 판정을 규정 기반 Rule Engine으로 자동화하는 서비스의 MVP 구현입니다.

기획 배경, 요구사항, DB 설계는 [`docs/`](docs) 폴더를 참고하세요.

- [docs/proposal.md](docs/proposal.md) — 참가 기획안
- [docs/requirements.md](docs/requirements.md) — 요구사항 정의서 v2 (FR-AUTH/MOB/DASH/RULE/SCN/AUD)
- [docs/db-design.md](docs/db-design.md) — DB 설계서 v2 (12개 테이블, UUID PK, JSONB Rule)
- [docs/contest-notice.md](docs/contest-notice.md) — 공모전 공고 요약

## 현재 구현 범위

요구사항 정의서 v2의 **P0 항목 전체(FR-AUTH, FR-MOB, FR-DASH, FR-RULE, FR-SCN)** 와 **P1 항목(FR-AUD)** 을
모두 구현했습니다. 확장 요구사항(국방동원정보체계 연계, FAQ 챗봇, 현장 신원확인, 알림 연계 등)은
14절 향후 확장 요구사항으로 남겨두었습니다.

| 요구사항 | 구현 위치 |
|---|---|
| FR-AUTH-001~003 로그인/권한/로그아웃 | `AuthController`, `SecurityConfig`, JWT (ADMIN/RESERVIST/DEMO) |
| FR-MOB-001~003 소집 회차 등록/대상자/입영 상태 | `AdminMasterDataController`(회차 등록 포함), `AdminTargetController`, `AttendanceService` |
| FR-MOB-004/005 예비군 본인 조회·소집통지 확인 | `MeController`(소집통지 확인 포함), `MeService` |
| FR-DASH-001~004 통합 대시보드/검색/상세 | `AdminDashboardController`, `DashboardService`, `TargetService` |
| FR-RULE-001~006 규정 기반 자동판정 및 임계값 관리 | `RuleEngine`(JSONB 조건 해석), `RuleEvaluationService`, `RuleAdminService`(코드 재배포 없는 임계값·버전 관리), `rules`/`rule_evaluations` 테이블 |
| FR-SCN-001~003 시나리오 선택/재생/초기화 | `DemoController`, `ScenarioService` |
| FR-AUD-001/002 상태변경·판정 이력 | `AuditLogService`, `AdminAuditLogController`, `AdminEvaluationController` |

핵심 설계 원칙(요구사항 정의서 6절)대로, 지연입소(1시간)·조기퇴소(100km) 같은 판정 기준은 코드에
하드코딩하지 않고 `rules` 테이블에 JSONB 조건(`{"field":"arrival_delay_minutes","operator":"LTE","value":60}`)
으로 저장하며, `RuleEngine`이 이를 해석해 실행합니다. 규정이 바뀌면 배포 없이 데이터만 바꾸면 됩니다.

## 아키텍처

```
backend/   Kotlin + Spring Boot 3.5, Gradle, PostgreSQL 16(UUID/JSONB), JWT 인증
  domain/           엔티티 + 룰엔진(RuleEngine, 순수 도메인 로직) + 상태 enum
  application/       유스케이스 서비스 (AuthService, AttendanceService, RuleEvaluationService,
                     DashboardService, TargetService, ScenarioService, MeService, AuditLogService)
  adapter/web/       REST 컨트롤러 + DTO (/api/v1/**)
  repository/        Spring Data JPA 리포지토리
  config/            보안(JWT/RBAC), 시드 데이터(DataSeeder), CORS 설정

frontend/  React 18 + Vite + TypeScript, react-router-dom 기반 라우팅, 반응형 웹
  src/api/          fetch 클라이언트 + 백엔드 DTO 타입
  src/context/       인증 컨텍스트 (JWT 저장/역할 분기)
  src/pages/admin/   관리자 화면 (대시보드, 판정 이력, 처리 이력, 시연 제어, 규정 관리)
  src/pages/reservist/  예비군 개인 화면 (소집통지 확인, 판정 결과)
```

DB 스키마는 `backend/src/main/resources/db/migration/V1__init_schema.sql`에 Flyway 마이그레이션으로
정의되어 있으며, `docs/db-design.md`의 12개 테이블(users, units, locations, reservists, mobilizations,
mobilization_targets, attendances, rules, rule_evaluations, scenarios, scenario_runs, audit_logs)과
인덱스 설계를 그대로 반영합니다. Kotlin 예약어 `Unit`과의 충돌을 피하기 위해 `units` 테이블에 대응하는
엔티티 클래스명만 `MilitaryUnit`으로 명명했습니다(테이블명은 동일).

### 오프라인 시연 설계 (FR-SCN / NFR-OFF-001)

2차 발표심사가 네트워크 제한 환경에서 진행될 수 있다는 공모 조건에 대응하기 위해, 외부 API 연동 없이
로컬 PostgreSQL만으로 완결되도록 설계했습니다.

- 앱 구동 시 `DataSeeder`가 마스터 데이터(부대 3개·장소 3개·규칙 4개·시나리오 4종·예비군 풀 100명·계정)를
  적재하고, 시연용 시나리오(`SCN-DELAY`)를 한 번 실행해 대시보드가 처음부터 비어있지 않도록 합니다.
- 발표 중에는 **시연 제어** 화면(SCR-07)에서 4가지 시나리오(정상/지연/예외/대규모 100명)를 버튼 한 번으로
  전환하거나, **1-Click 초기화**로 전체 시연 데이터를 되돌릴 수 있습니다 — 전부 로컬 DB 트랜잭션이며
  외부 호출이 없습니다.
- 데모 로그인 계정(`reservist01~03`)은 어떤 시나리오를 실행하더라도 항상 소집 대상에 포함되도록
  `ScenarioService`가 보장하여, 예비군 화면 시연이 막히지 않습니다.

## 실행 방법

### 1. 필수 도구
- Java 21+
- Node.js 18+
- PostgreSQL 16 (Docker 사용 권장)

### 2. 데이터베이스 기동

```bash
docker compose up -d postgres
```

Docker를 사용할 수 없는 환경이라면 로컬 PostgreSQL 16에 아래와 같이 데이터베이스/계정을 생성해도 됩니다.

```sql
CREATE USER reserveforces WITH PASSWORD 'reserveforces';
CREATE DATABASE reserveforces OWNER reserveforces;
```

### 3. 백엔드 실행

```bash
cd backend
./gradlew bootRun
```

- API: http://localhost:8080/api/v1
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- 최초 구동 시 Flyway가 스키마를 생성하고, `DataSeeder`가 마스터 데이터와 초기 시나리오를 자동 적재합니다.

### 4. 프론트엔드 실행

```bash
cd frontend
npm install
npm run dev
```

- http://localhost:5173 (Vite dev 서버가 `/api` 요청을 8080 백엔드로 프록시합니다)

### 데모 계정 (공통 비밀번호: `reserve1234!`)

| 구분 | 아이디 | 비고 |
|---|---|---|
| 관리자(ADMIN) | `admin` | 대시보드·판정 이력·처리 이력·시연 제어 전체 접근 |
| 시연 관리자(DEMO) | `demo` | `/api/v1/demo/**` (시나리오 실행/초기화) 전용 API 계정 |
| 예비군(RESERVIST) | `reservist01`, `reservist02`, `reservist03` | 어떤 시나리오를 실행해도 항상 소집 대상에 포함됨 |

## 테스트

```bash
cd backend
./gradlew test        # RuleEngine, AttendanceService, ScenarioService, RuleAdminService 단위 테스트

cd frontend
npm run build          # tsc 타입체크 + 프로덕션 빌드
```

관리자 화면의 **규정 관리** 메뉴에서 지연입소·조기퇴소 임계값을 직접 수정할 수 있습니다. 값을 바꾸면
서비스 코드 재배포 없이 즉시 새 기준으로 판정되며, 규칙 버전이 자동으로 한 단계 올라갑니다(FR-RULE-006).

## 폴더 구조

```
ReserveForces/
├── docs/                 기획/요구사항/DB 설계 문서
├── backend/              Kotlin + Spring Boot 3.5 백엔드 (Gradle)
├── frontend/             React 18 + Vite 프론트엔드
└── docker-compose.yml    로컬 PostgreSQL 16
```
