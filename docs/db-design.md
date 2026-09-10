# DB 설계서 v2 (Database Design Specification)

> 2026년 「예비전력분야 혁신 아이디어」 공모전 — 혁신 서비스 개발 분야
> **예비군 동원소집 통합 관제 시스템 (가칭)**

## 1. 설계 개요

| 항목 | 내용 |
|---|---|
| 대상 서비스 | 예비군 동원소집 통합 관제 시스템 |
| DBMS | PostgreSQL 16 |
| ORM | JPA / Hibernate (Kotlin) |
| 주요 사용자 | 부대 동원 담당자, 예비군 |
| 설계 방향 | 관계형 중심 + Rule Engine의 규칙/판정 근거는 JSONB 활용 |
| MVP 형태 | 모듈형 모놀리스, 로컬 시연 우선 |
| 핵심 목표 | 소집회차·대상자·입영상태·자동판정·시연 시나리오를 일관된 데이터 모델로 관리 |

요구사항 정의서의 핵심 엔티티(User, Mobilization, Reservist, MobilizationTarget, Attendance, Rule, RuleEvaluation, Scenario, AuditLog)를 실제 구현 가능한 관계형 모델로 구체화했다.

## 2. 데이터 설계 원칙

- 실제 개인정보·군사정보를 사용하지 않고 공모전 시연에서는 가명/더미 데이터를 사용한다.
- 업무의 기준이 되는 소집회차와 소집대상자를 중심으로 데이터 관계를 구성한다.
- 입영 기록과 판정 결과를 분리하여 '사실 데이터'와 '판정 데이터'를 구분한다.
- 규정이 변경될 수 있으므로 Rule과 RuleEvaluation에 버전 개념을 둔다.
- 판정에 사용된 입력값과 결과는 JSONB로 보존하여 나중에 동일 판정을 재현할 수 있도록 한다.
- FK, NOT NULL, UNIQUE, CHECK 등 DB 제약조건을 적극 사용해 데이터 무결성을 확보한다.
- 삭제보다는 상태 변경 및 이력 보존을 우선하며, 감사 추적이 필요한 데이터는 물리 삭제를 제한한다.

## 3. 논리 ERD

```
USER
 ├──< AUDIT_LOG
 │
 └──< MOBILIZATION_TARGET >── MOBILIZATION ──< SCENARIO_RUN
          │                         │
          │                         └── UNIT / LOCATION
          │
          ├── RESERVIST
          │
          ├── ATTENDANCE
          │
          └──< RULE_EVALUATION >── RULE

SCENARIO ──< SCENARIO_RUN
```

| 관계 | 카디널리티 | 설명 |
|---|---|---|
| User : AuditLog | 1:N | 사용자의 주요 행위 기록 |
| Mobilization : MobilizationTarget | 1:N | 한 소집회차에 여러 대상자 |
| Reservist : MobilizationTarget | 1:N | 한 예비군은 여러 소집회차에 대상자가 될 수 있음 |
| MobilizationTarget : Attendance | 1:1 | MVP에서는 소집대상별 대표 입영기록 1건 |
| MobilizationTarget : RuleEvaluation | 1:N | 한 대상자에 대해 규칙 평가가 여러 번 실행될 수 있음 |
| Rule : RuleEvaluation | 1:N | 한 규칙이 여러 대상자에게 적용될 수 있음 |
| Mobilization : ScenarioRun | 1:N | 특정 소집회차를 대상으로 여러 시연 실행 가능 |
| Scenario : ScenarioRun | 1:N | 하나의 시나리오를 반복 실행 가능 |

## 4. 테이블 목록

| No | 테이블 | 목적 | 유형 |
|---|---|---|---|
| 1 | users | 인증 사용자 및 역할 | Master |
| 2 | units | 소집부대 정보 | Master |
| 3 | locations | 소집/훈련 장소 정보 | Master |
| 4 | reservists | 예비군 기본 정보(시연용) | Master |
| 5 | mobilizations | 소집 회차 | Transaction |
| 6 | mobilization_targets | 소집 대상자 연결 및 상태 | Transaction |
| 7 | attendances | 입영 사실 기록 | Transaction |
| 8 | rules | 규정 기반 판정 규칙 | Rule |
| 9 | rule_evaluations | 규칙 실행 결과 및 근거 | Rule |
| 10 | scenarios | 발표용 시나리오 | Demo |
| 11 | scenario_runs | 시나리오 실행 기록 | Demo |
| 12 | audit_logs | 주요 행위 감사 로그 | Log |

## 5. 상세 테이블 설계

### 5.1 users
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | UUID | PK | 사용자 식별자 |
| login_id | VARCHAR(50) | UK, NN | 로그인 ID |
| password_hash | VARCHAR(255) | NN | BCrypt 해시 |
| name | VARCHAR(50) | NN | 표시명 |
| role | VARCHAR(20) | NN | ADMIN / RESERVIST / DEMO |
| status | VARCHAR(20) | NN | ACTIVE / INACTIVE |
| last_login_at | TIMESTAMPTZ | | 최근 로그인 |
| created_at | TIMESTAMPTZ | NN | 생성 시각 |
| updated_at | TIMESTAMPTZ | NN | 수정 시각 |

### 5.2 units
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | UUID | PK | 부대 ID |
| code | VARCHAR(30) | UK, NN | 시연용 부대 코드 |
| name | VARCHAR(100) | NN | 부대명 |
| region_code | VARCHAR(20) | | 지역 코드 |
| status | VARCHAR(20) | NN | ACTIVE / INACTIVE |
| created_at | TIMESTAMPTZ | NN | 생성 시각 |
| updated_at | TIMESTAMPTZ | NN | 수정 시각 |

### 5.3 locations
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | UUID | PK | 장소 ID |
| name | VARCHAR(100) | NN | 장소명 |
| address | VARCHAR(255) | | 시연용 주소 |
| latitude | NUMERIC(9,6) | | 위도 |
| longitude | NUMERIC(9,6) | | 경도 |
| location_type | VARCHAR(20) | NN | MOBILIZATION_SITE / TRAINING_SITE |
| created_at | TIMESTAMPTZ | NN | 생성 시각 |
| updated_at | TIMESTAMPTZ | NN | 수정 시각 |

### 5.4 reservists
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | UUID | PK | 예비군 ID |
| user_id | UUID | FK, UK, NN | 연결 사용자 |
| demo_identifier | VARCHAR(30) | UK, NN | 시연용 식별번호 |
| name | VARCHAR(50) | NN | 가명 |
| address_region | VARCHAR(100) | | 거주지역(시연용) |
| status | VARCHAR(20) | NN | ACTIVE / INACTIVE |
| created_at | TIMESTAMPTZ | NN | 생성 시각 |
| updated_at | TIMESTAMPTZ | NN | 수정 시각 |

### 5.5 mobilizations
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | UUID | PK | 소집 회차 ID |
| unit_id | UUID | FK, NN | 소집부대 |
| location_id | UUID | FK, NN | 소집 장소 |
| name | VARCHAR(100) | NN | 소집 회차명 |
| scheduled_start_at | TIMESTAMPTZ | NN | 소집 시작 |
| scheduled_end_at | TIMESTAMPTZ | | 소집 종료 |
| status | VARCHAR(20) | NN | PLANNED / IN_PROGRESS / COMPLETED / CANCELLED |
| created_at | TIMESTAMPTZ | NN | 생성 시각 |
| updated_at | TIMESTAMPTZ | NN | 수정 시각 |

### 5.6 mobilization_targets
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | UUID | PK | 소집 대상 ID |
| mobilization_id | UUID | FK, NN | 소집 회차 |
| reservist_id | UUID | FK, NN | 예비군 |
| target_status | VARCHAR(30) | NN | EXPECTED / ARRIVED / DELAYED / ABSENT / EXCEPTION / COMPLETED |
| notice_confirmed_at | TIMESTAMPTZ | | 소집통지 확인 시각 |
| created_at | TIMESTAMPTZ | NN | 생성 시각 |
| updated_at | TIMESTAMPTZ | NN | 수정 시각 |

### 5.7 attendances
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | UUID | PK | 입영 기록 ID |
| target_id | UUID | FK, UK, NN | 소집 대상 |
| scheduled_at | TIMESTAMPTZ | NN | 예정 입영 시각 |
| arrived_at | TIMESTAMPTZ | | 실제 입영 시각 |
| departure_at | TIMESTAMPTZ | | 퇴소 시각 |
| distance_km | NUMERIC(8,2) | | 판정에 사용할 거리 |
| attendance_status | VARCHAR(30) | NN | PENDING / NORMAL / DELAY / EXCEPTION |
| recorded_by | UUID | FK | 처리 관리자 |
| created_at | TIMESTAMPTZ | NN | 생성 시각 |
| updated_at | TIMESTAMPTZ | NN | 수정 시각 |

### 5.8 rules
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | UUID | PK | 규칙 ID |
| code | VARCHAR(50) | UK, NN | 규칙 코드 |
| name | VARCHAR(100) | NN | 규칙명 |
| version | VARCHAR(30) | NN | 규칙 버전 |
| description | TEXT | NN | 규칙 설명 |
| rule_type | VARCHAR(30) | NN | TIME / DISTANCE / COMPOSITE / OTHER |
| conditions | JSONB | NN | 조건식/기준값 |
| result_code | VARCHAR(30) | NN | 판정 결과 코드 |
| priority | INTEGER | NN | 규칙 우선순위 |
| effective_from | DATE | | 적용 시작일 |
| effective_to | DATE | | 적용 종료일 |
| enabled | BOOLEAN | NN | 활성 여부 |
| created_at | TIMESTAMPTZ | NN | 생성 시각 |
| updated_at | TIMESTAMPTZ | NN | 수정 시각 |

### 5.9 rule_evaluations
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | UUID | PK | 판정 실행 ID |
| target_id | UUID | FK, NN | 소집 대상 |
| rule_id | UUID | FK, NN | 적용 규칙 |
| rule_version | VARCHAR(30) | NN | 실행 당시 규칙 버전 |
| input_data | JSONB | NN | 판정 입력값 스냅샷 |
| result_code | VARCHAR(30) | NN | 판정 결과 |
| result_message | VARCHAR(500) | | 사용자 설명 |
| evaluated_at | TIMESTAMPTZ | NN | 판정 시각 |
| evaluated_by | UUID | FK | 실행 사용자 |

### 5.10 scenarios
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | UUID | PK | 시나리오 ID |
| code | VARCHAR(50) | UK, NN | 시나리오 코드 |
| name | VARCHAR(100) | NN | 시나리오명 |
| description | TEXT | | 시나리오 설명 |
| dataset | JSONB | NN | 시연용 데이터 |
| enabled | BOOLEAN | NN | 사용 여부 |
| created_at | TIMESTAMPTZ | NN | 생성 시각 |
| updated_at | TIMESTAMPTZ | NN | 수정 시각 |

### 5.11 scenario_runs
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | UUID | PK | 실행 ID |
| scenario_id | UUID | FK, NN | 시나리오 |
| mobilization_id | UUID | FK | 대상 소집 회차 |
| run_status | VARCHAR(20) | NN | RUNNING / COMPLETED / RESET |
| started_at | TIMESTAMPTZ | NN | 실행 시작 |
| completed_at | TIMESTAMPTZ | | 종료 |
| executed_by | UUID | FK | 시연자 |

### 5.12 audit_logs
| 컬럼 | 타입 | 제약 | 설명 |
|---|---|---|---|
| id | UUID | PK | 로그 ID |
| actor_id | UUID | FK | 행위자 |
| action | VARCHAR(50) | NN | CREATE/UPDATE/LOGIN/EVALUATE 등 |
| target_type | VARCHAR(50) | NN | 대상 리소스 유형 |
| target_id | UUID | | 대상 ID |
| before_data | JSONB | | 변경 전 데이터 |
| after_data | JSONB | | 변경 후 데이터 |
| created_at | TIMESTAMPTZ | NN | 행위 시각 |

## 6. 주요 제약조건 및 무결성

| 대상 | 제약 | 목적 |
|---|---|---|
| users.login_id | UNIQUE | 로그인 ID 중복 방지 |
| reservists.user_id | UNIQUE + FK | 예비군 사용자와 1:1 |
| mobilization_targets(mobilization_id, reservist_id) | UNIQUE | 동일 회차에 동일 예비군 중복 등록 방지 |
| attendances.target_id | UNIQUE + FK | MVP에서는 소집 대상당 대표 입영기록 1건 |
| rules.code | UNIQUE | 규칙 코드 식별 |
| scenarios.code | UNIQUE | 시나리오 코드 식별 |
| mobilizations.scheduled_end_at | CHECK | 종료 시각이 시작보다 빠르지 않도록 검증 |
| rules.priority | CHECK | 0 이상의 우선순위 |

## 7. 인덱스 설계

| 테이블 | 인덱스 | 컬럼 | 목적 |
|---|---|---|---|
| users | idx_users_role | role | 관리자/예비군 조회 |
| mobilizations | idx_mobilizations_status_start | status, scheduled_start_at | 진행 중 회차 및 일정 조회 |
| mobilization_targets | idx_targets_mobilization_status | mobilization_id, target_status | 관제 상태별 집계 |
| mobilization_targets | idx_targets_reservist | reservist_id | 개인 소집 이력 조회 |
| attendances | idx_attendance_arrived_at | arrived_at | 입영 시간대 조회 |
| rule_evaluations | idx_eval_target_time | target_id, evaluated_at DESC | 대상자 판정 이력 |
| rule_evaluations | idx_eval_result | result_code | 예외/지연 건 조회 |
| rules | idx_rules_enabled | enabled, rule_type | 활성 규칙 탐색 |
| rules | idx_rules_conditions_gin | conditions USING GIN | JSONB 조건 검색 |
| scenarios | idx_scenarios_enabled | enabled | 사용 가능한 시나리오 조회 |
| audit_logs | idx_audit_target | target_type, target_id, created_at DESC | 대상별 감사 이력 |

## 8. 시연용 데이터 모델

| 시나리오 | 대상 인원 | 핵심 데이터 변화 | 시연 목적 |
|---|---|---|---|
| SCN-NORMAL | 20명 | 정상 입영 위주 | 기본 관제 흐름 |
| SCN-DELAY | 20명 | 일부 실제 입영 시각 변경 | Rule Engine 자동판정 |
| SCN-EXCEPTION | 20명 | 필수 입력값 누락/복합 조건 | 담당자 확인 대상 |
| SCN-LARGE | 100명 | 대규모 입영 현황 | 관제 대시보드의 실용성 |

시나리오 데이터는 실제 군 관련 개인정보를 사용하지 않고 가명 및 합성 데이터로 구성한다.

## 9. Rule 데이터 구조 예시

```json
{
  "conditions": {
    "arrival_delay": {
      "operator": "LTE",
      "value": 60,
      "unit": "MINUTE"
    }
  },
  "exceptions": [],
  "metadata": {
    "source": "OFFICIAL_RULE_REFERENCE",
    "verifiedAt": "YYYY-MM-DD"
  }
}
```

※ 위 수치 60분은 DB 구조 예시일 뿐 실제 적용 기준을 확정한 값이 아니다. 공모전 제출/서비스 구현 시 최신 공식 규정 확인 후 입력한다. 규정 변경에 대비해 `source`, `verifiedAt`, `version` 등의 메타데이터를 함께 관리한다.

본 저장소의 실제 구현은 `rule_type`에 따라 `conditions.field` / `operator` / `value` / `unit` 구조를 사용한다 (자세한 내용은 `backend`의 `RuleEngine` 참고).

## 10. 관제 화면 핵심 조회 요구사항

| 조회 | 주요 테이블 | 필터/집계 |
|---|---|---|
| 전체 소집 인원 | mobilization_targets | mobilization_id |
| 입영 완료 인원 | mobilization_targets + attendances | target_status/attendance_status |
| 지연 인원 | mobilization_targets + attendances | DELAY |
| 예외 인원 | mobilization_targets + rule_evaluations | EXCEPTION |
| 개인 소집 이력 | reservists + mobilization_targets + mobilizations | reservist_id |
| 판정 근거 | rule_evaluations + rules | target_id, evaluated_at |
| 시간대별 입영률 | attendances | arrived_at |

## 11. 상태 전이

```
소집 대상
EXPECTED
   │
   ├── 입영 확인 ──> ARRIVED
   │                    │
   │                    └── 판정 ──> NORMAL / DELAY / EXCEPTION
   │
   ├── 미입영 처리 ──> ABSENT
   │
   └── 종료 처리 ──> COMPLETED
```

상태 전이 자체와 Rule Engine의 판정 결과를 분리한다. 예를 들어 ARRIVED는 '입영했다'는 사실이고, DELAY는 '규칙 적용 결과 지연으로 분류되었다'는 의미다.

## 12. JPA 엔티티 설계 방향

- Entity 간 양방향 연관관계는 최소화하고, 기본적으로 다대일(N:1) 단방향 연관관계를 우선한다.
- ID는 UUID 기반으로 설계하여 시연 데이터 생성과 분산 확장에 유리하게 한다.
- createdAt/updatedAt은 공통 BaseEntity로 관리한다.
- Enum은 DB에 문자열 코드로 저장하여 가독성과 변경 대응력을 높인다.
- JSONB는 Rule.conditions, RuleEvaluation.inputData, Scenario.dataset, AuditLog의 before/after 데이터에 한정해 사용한다.
- 집계용 값은 원천 데이터와 중복 저장하지 않고 조회 시 계산한다. 성능 문제가 확인될 경우 별도 캐시/집계 구조를 검토한다.

## 13. 마이그레이션 및 초기 데이터

| 순서 | 작업 | 내용 |
|---|---|---|
| V1 | 기본 사용자/부대/장소 | users, units, locations |
| V2 | 소집 도메인 | reservists, mobilizations, mobilization_targets, attendances |
| V3 | Rule Engine | rules, rule_evaluations |
| V4 | 시연 | scenarios, scenario_runs |
| V5 | 감사 | audit_logs + indexes |

Flyway를 이용해 DB 스키마 변경 이력을 버전 관리한다. (본 저장소는 V1 단일 마이그레이션에 전체 스키마를 포함하고, 마스터/시연 데이터는 애플리케이션 시드 로직으로 적재한다 — 자세한 내용은 README 참고)

## 14. 개인정보 및 보안 설계

- 공모전 시연 DB에는 실제 주민등록번호, 군번, 실주소 등 민감한 식별정보를 저장하지 않는다.
- 예비군 식별은 `demo_identifier` 등 가상 식별자를 사용한다.
- 비밀번호는 평문 저장 금지 (BCrypt 해시).
- 관리자/예비군 권한은 애플리케이션 계층(Spring Security)과 API 접근제어로 제한한다.
- AuditLog에는 로그인, 상태 변경, 판정 실행 등 주요 행위를 남긴다.
- 실제 운영 전환 시 개인정보 최소수집, 보유기간, 암호화, 접근통제 등 운영 보안정책을 별도로 수립한다.

## 15. DB 설계 수용 기준

1. 소집회차 1건에 여러 소집대상자를 연결할 수 있다.
2. 한 예비군은 여러 소집회차에 참여할 수 있다.
3. 동일 회차에 동일 예비군이 중복 등록되지 않는다.
4. 입영 사실 데이터와 판정 결과 데이터가 분리되어 저장된다.
5. 한 대상자에 대해 여러 번 Rule Engine을 실행할 수 있고 실행 이력이 남는다.
6. 판정 결과에서 실행 당시 Rule 버전을 확인할 수 있다.
7. 관제 화면의 상태별 인원 집계가 원천 데이터와 일치한다.
8. 발표용 시나리오를 실행하고 초기화할 수 있다.
9. 인터넷이 없는 로컬 환경에서도 핵심 DB 조회와 판정 기능이 동작한다.
10. 실제 개인정보 없이 전체 발표 시나리오를 재현할 수 있다.

## 16. 최종 권장 구조

```
[사용자]
    │
    ├── 관리자 ──> 소집회차 ──> 소집대상 ──> 입영기록
    │                           │
    │                           └──> Rule Engine ──> 판정이력
    │
    └── 예비군 ──> 본인 소집대상 조회

[발표 시연]
Scenario ──> ScenarioRun ──> 소집/대상/입영 데이터 ──> 관제 화면
```

이 구조는 1인 개발 MVP에 필요한 데이터 모델을 과도하게 복잡하게 만들지 않으면서, 향후 실제 외부 체계 연계·챗봇·현장 신원확인 모듈을 추가할 수 있는 확장 여지를 남긴다.

---

> **v1 대비 변경 이력**: 7개 테이블(BIGINT PK) → 12개 테이블(UUID PK) 구조로 확장했다. 판정 기준을 `rules`(JSONB 조건) 테이블로 데이터화하고, 판정 결과를 `rule_evaluations`에 입력값 스냅샷과 함께 기록하도록 변경했다. 시연 전용 `scenarios`/`scenario_runs` 테이블과 `audit_logs`를 신설했다. 이전 버전은 git 히스토리에서 확인할 수 있다.
