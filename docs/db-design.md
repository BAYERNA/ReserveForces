# DB 설계서 (Database Design Specification)

> 2026년 「예비전력분야 혁신 아이디어」 공모전 — 혁신 서비스 개발 분야
> **예비군 동원소집 통합 관제 시스템**

| 문서버전 | v1.0 | 작성일 | 2026-09-02 |
|---|---|---|---|
| DBMS | PostgreSQL 16 | 관련 문서 | 요구사항 정의서 v1.0 |

## 1. 개요

### 1-1. 목적
본 문서는 '예비군 동원소집 통합 관제 시스템'의 요구사항 정의서(FR-01~13)를 기반으로 데이터베이스 구조를 정의한다. 관리자(부대 동원 담당자)와 일반 사용자(소집대상 예비군)의 기능 요구사항을 지원하는 테이블, 컬럼, 관계, 제약조건을 명세한다.

### 1-2. 설계 원칙
- 모든 인적 데이터는 실제 개인정보가 아닌 시연용 가상 데이터로 구성한다 (NFR-보안 대응).
- 소집부대·훈련장 정보를 테이블로 분리하여 코드 수정 없이 타 부대로 확장 가능하도록 한다 (NFR-이식성 대응).
- 판정 이력(JUDGMENT_RESULT)과 처리 이력(PROCESS_LOG)을 분리하여 자동판정 결과와 사람에 의한 처리 기록을 함께 추적할 수 있도록 한다 (FR-06, FR-10 대응).
- 오프라인 시연 모드(FR-11)를 지원하기 위해 모든 테이블은 외부 시스템 연동 없이 자체 시드 데이터로 초기화 가능한 구조로 설계한다.

## 2. ERD 요약

```
UNIT (1) ──< (N) RESERVIST (1) ──< (N) CALLUP_NOTICE
  │                  │
  │                  ├──< (N) ENTRY_RECORD
  │                  │
  │                  └──< (N) JUDGMENT_RESULT ──< (N) PROCESS_LOG >── (1) USER_ACCOUNT
  │
  └──< (N) USER_ACCOUNT (role=ADMIN)

RESERVIST (1) ── (1) USER_ACCOUNT (role=RESERVIST)
```

※ PK=기본키, FK=외래키. 화살표는 1:N 방향(부모→자식)을 의미

## 3. 테이블 정의

### USER_ACCOUNT (사용자계정)
관련 요구사항: FR-01

| 컬럼명 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| user_id | BIGINT | PK | 사용자 고유 식별자 |
| login_id | VARCHAR(50) | UNIQUE, NOT NULL | 로그인 아이디 |
| password_hash | VARCHAR(255) | NOT NULL | 비밀번호 해시값 |
| role | VARCHAR(20) | NOT NULL, CHECK(ADMIN/RESERVIST) | 계정 역할 — 관리자 또는 일반 사용자 |
| unit_id | BIGINT | FK → UNIT.unit_id, NULL 허용 | 관리자 계정의 소속 부대 (일반 사용자는 NULL) |
| reservist_id | BIGINT | FK → RESERVIST.reservist_id, NULL 허용 | 일반 사용자 계정과 연결된 예비군 (관리자는 NULL) |
| created_at | TIMESTAMP | NOT NULL, DEFAULT now() | 계정 생성일시 |

### UNIT (소집부대/훈련장)
관련 요구사항: FR-02, FR-05, FR-08

| 컬럼명 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| unit_id | BIGINT | PK | 부대 고유 식별자 |
| unit_name | VARCHAR(100) | NOT NULL | 부대명 |
| location | VARCHAR(200) | NOT NULL | 부대·훈련장 위치 |
| entry_deadline_time | TIME | NOT NULL | 지정 입영시각 (지연입소 판정 기준) |
| contact | VARCHAR(50) | | 부대 연락처 |

### RESERVIST (소집대상자)
관련 요구사항: FR-02, FR-04, FR-05

| 컬럼명 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| reservist_id | BIGINT | PK | 소집대상자 고유 식별자 |
| display_name | VARCHAR(50) | NOT NULL | 표시용 성명 (시연용 가상 데이터) |
| residence_distance_km | NUMERIC(6,1) | NOT NULL | 거주지-소집부대 간 거리(km) — 조기퇴소 판정 기준 |
| unit_id | BIGINT | FK → UNIT.unit_id, NOT NULL | 소집 대상 부대 |
| status | VARCHAR(20) | NOT NULL, DEFAULT '소집전' | 현재 상태(소집전/입영/지연/조기퇴소/완료) |

### CALLUP_NOTICE (소집통지)
관련 요구사항: FR-07

| 컬럼명 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| notice_id | BIGINT | PK | 소집통지 고유 식별자 |
| reservist_id | BIGINT | FK → RESERVIST.reservist_id, NOT NULL | 통지 대상 예비군 |
| unit_id | BIGINT | FK → UNIT.unit_id, NOT NULL | 입영 대상 부대 |
| scheduled_datetime | TIMESTAMP | NOT NULL | 소집 예정 일시 |
| notice_status | VARCHAR(20) | NOT NULL, DEFAULT '발송완료' | 통지 상태 |

### ENTRY_RECORD (입영기록)
관련 요구사항: FR-02, FR-03

| 컬럼명 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| entry_id | BIGINT | PK | 입영기록 고유 식별자 |
| reservist_id | BIGINT | FK → RESERVIST.reservist_id, NOT NULL | 입영 대상 예비군 |
| actual_entry_datetime | TIMESTAMP | NULL 허용(미입영 시) | 실제 입영 일시 |
| entry_status | VARCHAR(20) | NOT NULL, DEFAULT '대기' | 입영 상태(대기/완료/지연/미입영) |

### JUDGMENT_RESULT (판정결과)
관련 요구사항: FR-04, FR-05, FR-06, FR-09

| 컬럼명 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| judgment_id | BIGINT | PK | 판정결과 고유 식별자 |
| reservist_id | BIGINT | FK → RESERVIST.reservist_id, NOT NULL | 판정 대상 예비군 |
| judgment_type | VARCHAR(20) | NOT NULL, CHECK(지연입소/조기퇴소) | 판정 유형 |
| is_auto | BOOLEAN | NOT NULL, DEFAULT true | 자동판정 여부 (false=수동 보정) |
| result | VARCHAR(10) | NOT NULL, CHECK(허용/불허) | 판정 결과 |
| reason | VARCHAR(255) | NULL 허용 | 수동 보정 시 사유 (FR-06) |
| judged_at | TIMESTAMP | NOT NULL, DEFAULT now() | 판정 처리 일시 |

### PROCESS_LOG (처리이력)
관련 요구사항: FR-10

| 컬럼명 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| log_id | BIGINT | PK | 이력 고유 식별자 |
| judgment_id | BIGINT | FK → JUDGMENT_RESULT.judgment_id, NOT NULL | 관련 판정결과 |
| user_id | BIGINT | FK → USER_ACCOUNT.user_id, NOT NULL | 처리자(관리자) 계정 |
| processed_at | TIMESTAMP | NOT NULL, DEFAULT now() | 처리 일시 |
| content | VARCHAR(255) | NOT NULL | 처리 내용 |

## 4. 테이블 간 관계

| 부모 테이블 | 관계 | 자식 테이블 | 설명 |
|---|---|---|---|
| UNIT | 1:N | RESERVIST | 한 부대에 다수의 소집대상자가 소속됨 |
| UNIT | 1:N | USER_ACCOUNT | 한 부대에 다수의 관리자 계정이 존재 가능 |
| RESERVIST | 1:1 | USER_ACCOUNT | 예비군 1명은 1개의 일반 사용자 계정과 연결 |
| RESERVIST | 1:N | CALLUP_NOTICE | 한 예비군에게 여러 회차의 소집통지가 발송될 수 있음 |
| RESERVIST | 1:N | ENTRY_RECORD | 한 예비군의 입영기록(회차별) |
| RESERVIST | 1:N | JUDGMENT_RESULT | 한 예비군에 대해 지연입소·조기퇴소 판정이 각각 발생 가능 |
| JUDGMENT_RESULT | 1:N | PROCESS_LOG | 한 판정결과에 대해 여러 차례 처리(보정)가 기록될 수 있음 |
| USER_ACCOUNT | 1:N | PROCESS_LOG | 한 관리자가 여러 처리 이력을 남길 수 있음 |

## 5. 인덱스 설계

| 테이블 | 인덱스 대상 컬럼 | 목적 |
|---|---|---|
| RESERVIST | unit_id, status | 관리자 대시보드에서 부대·상태별 현황 조회 성능 확보 (FR-02, FR-03) |
| CALLUP_NOTICE | reservist_id | 본인 소집통지 조회 성능 확보 (FR-07) |
| ENTRY_RECORD | reservist_id, entry_status | 입영 진행률 집계 성능 확보 (FR-03) |
| JUDGMENT_RESULT | reservist_id, judgment_type | 판정 결과 조회 및 유형별 집계 성능 확보 (FR-04, FR-05, FR-09) |
| PROCESS_LOG | judgment_id | 판정결과별 처리 이력 조회 성능 확보 (FR-10) |

## 6. 시드 데이터 및 시연 전략

2차 발표심사는 네트워크가 제한되는 환경에서도 시연이 가능해야 하므로(NFR-가용성), 모든 테이블은 애플리케이션 구동 시 자체 시드 데이터로 초기화되도록 설계한다.

- UNIT: 2~3개 가상 부대·훈련장 데이터를 시드로 등록 (지정 입영시각, 위치 상이하게 구성)
- RESERVIST: 지연입소·조기퇴소 각 케이스가 고르게 나타나도록 거리값·상태를 분산하여 20~30건 시드 등록
- CALLUP_NOTICE / ENTRY_RECORD: RESERVIST 시드에 대응하는 통지·입영 데이터를 함께 생성
- JUDGMENT_RESULT: 애플리케이션 구동 시 룰엔진이 RESERVIST·ENTRY_RECORD 시드 데이터를 기준으로 자동 산출 (실시간 판정 로직 시연용)

※ 시드 데이터의 display_name은 모두 가상 이름으로 생성하며 실제 개인정보를 사용하지 않는다.

> 본 저장소의 구현은 `backend/src/main/resources/db/migration/V1__init_schema.sql`에서 스키마를 정의하고, `DataSeeder` 컴포넌트가 앱 구동 시 위 전략에 따라 시드 데이터를 적재하고 룰엔진으로 판정결과를 산출한다.
