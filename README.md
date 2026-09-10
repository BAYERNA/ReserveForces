# ReserveForces — 예비군 동원소집 통합 관제 시스템

2026년 「예비전력분야 혁신 아이디어」 공모전(혁신 서비스 개발 분야) 출품작.
부대 담당자용 실시간 관제 대시보드와 예비군용 소집 확인 화면을 하나의 웹 서비스로 연동하고,
지연입소·조기퇴소 판정을 규정 기반 룰엔진으로 자동화하는 서비스의 MVP 구현입니다.

기획 배경, 요구사항, DB 설계는 [`docs/`](docs) 폴더를 참고하세요.

- [docs/proposal.md](docs/proposal.md) — 참가 기획안
- [docs/requirements.md](docs/requirements.md) — 요구사항 정의서 (FR-01~13)
- [docs/db-design.md](docs/db-design.md) — DB 설계서
- [docs/contest-notice.md](docs/contest-notice.md) — 공모전 공고 요약

## 현재 구현 범위

요구사항 정의서 기준 **Must + Should (FR-01~11)**를 MVP로 구현했습니다. FR-12(챗봇), FR-13(현장 신원확인)은
향후 확장 로드맵으로 남겨두었습니다 (기존 도메인 로직을 건드리지 않고 부가 모듈로 연결 가능한 구조).

| 요구사항 | 구현 위치 |
|---|---|
| FR-01 로그인/RBAC | `backend` `AuthController`, `SecurityConfig`, JWT |
| FR-02 소집대상자 현황 조회 | `AdminReservistController`, `AdminDashboardPage` |
| FR-03 입영 진행률 대시보드 | `AdminDashboardController`, `AdminDashboardPage` |
| FR-04 지연입소 자동판정 | `JudgmentRuleEngine`, `JudgmentService#recordEntryAndJudgeLateEntry` |
| FR-05 조기퇴소 자동판정 | `JudgmentRuleEngine`, `JudgmentService#createEarlyDischargeJudgment` |
| FR-06 판정 결과 수동 보정 | `JudgmentService#correctJudgment`, `AdminJudgmentsPage` |
| FR-07 소집통지 확인(개인) | `MeController`, `ReservistNoticePage` |
| FR-08 입영 부대·훈련장 정보 조회 | `MeController#myUnit`, `ReservistNoticePage` |
| FR-09 본인 판정 결과 확인 | `MeController#myJudgments`, `ReservistJudgmentPage` |
| FR-10 처리 이력 로그 | `ProcessLog`, `AdminProcessLogPage` |
| FR-11 시나리오 데이터 시연 모드 | `DataSeeder` (앱 구동 시 자체 시드 데이터 적재, 외부 연동 없음) |

## 아키텍처

```
backend/   Spring Boot 3.5, DDD/헥사고날 지향 레이어드 아키텍처, PostgreSQL 16, JWT 인증
  domain/          엔티티 + 판정 룰엔진(JudgmentRuleEngine, 순수 도메인 로직)
  application/      유스케이스 서비스 (AuthService, JudgmentService, ReservistQueryService, NoticeService)
  adapter/web/      REST 컨트롤러 + DTO
  repository/       Spring Data JPA 리포지토리
  config/           보안(JWT/RBAC), 시드 데이터(DataSeeder), CORS 설정

frontend/  React 18 + Vite + TypeScript, react-router-dom 기반 라우팅, 반응형 웹
  src/api/          fetch 클라이언트 + 백엔드 DTO 타입
  src/context/       인증 컨텍스트 (JWT 저장/역할 분기)
  src/pages/admin/   관리자 화면 (대시보드, 판정 처리, 처리 이력)
  src/pages/reservist/  예비군 개인 화면 (소집통지, 판정 결과)
```

DB 스키마는 `backend/src/main/resources/db/migration/V1__init_schema.sql`에 Flyway 마이그레이션으로 정의되어
있으며, `docs/db-design.md`의 7개 테이블(UNIT, RESERVIST, USER_ACCOUNT, CALLUP_NOTICE, ENTRY_RECORD,
JUDGMENT_RESULT, PROCESS_LOG) 및 인덱스 설계를 그대로 반영합니다.

### 오프라인 시연 설계 (FR-11 / NFR-가용성)

2차 발표심사가 네트워크 제한 환경에서 진행될 수 있다는 공모 조건에 대응하기 위해, 외부 API 연동 없이
로컬 PostgreSQL만으로 완결되도록 설계했습니다. `DataSeeder`가 앱 구동 시 3개 부대·24명의 소집대상자
시나리오 데이터(지연입소 허용/불허, 조기퇴소 허용/불허, 미입영 케이스가 고르게 분포)를 적재하고,
`JudgmentRuleEngine`으로 판정결과까지 미리 산출해 둡니다.

## 실행 방법

### 1. 필수 도구
- Java 21+, Maven 3.9+
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
./mvnw spring-boot:run   # 또는 mvn spring-boot:run
```

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- 최초 구동 시 Flyway가 스키마를 생성하고, `DataSeeder`가 시연용 데이터를 자동 적재합니다.

### 4. 프론트엔드 실행

```bash
cd frontend
npm install
npm run dev
```

- http://localhost:5173 (Vite dev 서버가 `/api` 요청을 8080 백엔드로 프록시합니다)

### 데모 계정 (공통 비밀번호: `reserve1234!`)

| 구분 | 아이디 |
|---|---|
| 관리자 | `admin` |
| 예비군(개인) | `reservist01`, `reservist02`, `reservist03` |

## 테스트

```bash
cd backend
mvn test          # JudgmentRuleEngine 단위 테스트 등

cd frontend
npm run build      # tsc 타입체크 + 프로덕션 빌드
```

## 폴더 구조

```
ReserveForces/
├── docs/                 기획/요구사항/DB 설계 문서
├── backend/              Spring Boot 3.5 백엔드
├── frontend/             React 18 + Vite 프론트엔드
└── docker-compose.yml    로컬 PostgreSQL 16
```
