-- 예비군 동원소집 통합 관제 시스템 - 초기 스키마 v2
-- docs/db-design.md 기준 (DBMS: PostgreSQL 16, PK: UUID)

-- gen_random_uuid()는 PostgreSQL 13+ 코어에 내장되어 있으나, 하위 호환을 위해 pgcrypto도 보장해 둔다.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    login_id       VARCHAR(50)  NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    name           VARCHAR(50)  NOT NULL,
    role           VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'RESERVIST', 'DEMO')),
    status         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    last_login_at  TIMESTAMPTZ,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE units (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(30)  NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    region_code VARCHAR(20),
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE locations (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(100) NOT NULL,
    address       VARCHAR(255),
    latitude      NUMERIC(9,6),
    longitude     NUMERIC(9,6),
    location_type VARCHAR(20)  NOT NULL CHECK (location_type IN ('MOBILIZATION_SITE', 'TRAINING_SITE')),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE reservists (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID         NOT NULL UNIQUE REFERENCES users (id),
    demo_identifier VARCHAR(30)  NOT NULL UNIQUE,
    name            VARCHAR(50)  NOT NULL,
    address_region  VARCHAR(100),
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE mobilizations (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    unit_id             UUID         NOT NULL REFERENCES units (id),
    location_id         UUID         NOT NULL REFERENCES locations (id),
    name                VARCHAR(100) NOT NULL,
    scheduled_start_at  TIMESTAMPTZ  NOT NULL,
    scheduled_end_at    TIMESTAMPTZ,
    status              VARCHAR(20)  NOT NULL DEFAULT 'PLANNED'
        CHECK (status IN ('PLANNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CHECK (scheduled_end_at IS NULL OR scheduled_end_at >= scheduled_start_at)
);

CREATE TABLE mobilization_targets (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mobilization_id      UUID        NOT NULL REFERENCES mobilizations (id),
    reservist_id         UUID        NOT NULL REFERENCES reservists (id),
    target_status        VARCHAR(30) NOT NULL DEFAULT 'EXPECTED'
        CHECK (target_status IN ('EXPECTED', 'ARRIVED', 'DELAYED', 'ABSENT', 'EXCEPTION', 'COMPLETED')),
    notice_confirmed_at  TIMESTAMPTZ,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (mobilization_id, reservist_id)
);

CREATE TABLE attendances (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    target_id          UUID        NOT NULL UNIQUE REFERENCES mobilization_targets (id),
    scheduled_at       TIMESTAMPTZ NOT NULL,
    arrived_at         TIMESTAMPTZ,
    departure_at       TIMESTAMPTZ,
    distance_km        NUMERIC(8,2),
    attendance_status  VARCHAR(30) NOT NULL DEFAULT 'PENDING'
        CHECK (attendance_status IN ('PENDING', 'NORMAL', 'DELAY', 'EXCEPTION')),
    recorded_by        UUID REFERENCES users (id),
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE rules (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(50)  NOT NULL UNIQUE,
    name            VARCHAR(100) NOT NULL,
    version         VARCHAR(30)  NOT NULL,
    description     TEXT         NOT NULL,
    rule_type       VARCHAR(30)  NOT NULL CHECK (rule_type IN ('TIME', 'DISTANCE', 'COMPOSITE', 'OTHER')),
    conditions      JSONB        NOT NULL,
    result_code     VARCHAR(30)  NOT NULL,
    priority        INTEGER      NOT NULL CHECK (priority >= 0),
    effective_from  DATE,
    effective_to    DATE,
    enabled         BOOLEAN      NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE rule_evaluations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    target_id       UUID         NOT NULL REFERENCES mobilization_targets (id),
    rule_id         UUID         NOT NULL REFERENCES rules (id),
    rule_version    VARCHAR(30)  NOT NULL,
    input_data      JSONB        NOT NULL,
    result_code     VARCHAR(30)  NOT NULL,
    result_message  VARCHAR(500),
    evaluated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    evaluated_by    UUID REFERENCES users (id)
);

CREATE TABLE scenarios (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code         VARCHAR(50)  NOT NULL UNIQUE,
    name         VARCHAR(100) NOT NULL,
    description  TEXT,
    dataset      JSONB        NOT NULL,
    enabled      BOOLEAN      NOT NULL DEFAULT true,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE scenario_runs (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    scenario_id       UUID        NOT NULL REFERENCES scenarios (id),
    mobilization_id   UUID REFERENCES mobilizations (id),
    run_status        VARCHAR(20) NOT NULL CHECK (run_status IN ('RUNNING', 'COMPLETED', 'RESET')),
    started_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at      TIMESTAMPTZ,
    executed_by       UUID REFERENCES users (id)
);

CREATE TABLE audit_logs (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id     UUID REFERENCES users (id),
    action       VARCHAR(50) NOT NULL,
    target_type  VARCHAR(50) NOT NULL,
    target_id    UUID,
    before_data  JSONB,
    after_data   JSONB,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- 인덱스 설계 (docs/db-design.md 7절)
CREATE INDEX idx_users_role ON users (role);
CREATE INDEX idx_mobilizations_status_start ON mobilizations (status, scheduled_start_at);
CREATE INDEX idx_targets_mobilization_status ON mobilization_targets (mobilization_id, target_status);
CREATE INDEX idx_targets_reservist ON mobilization_targets (reservist_id);
CREATE INDEX idx_attendance_arrived_at ON attendances (arrived_at);
CREATE INDEX idx_eval_target_time ON rule_evaluations (target_id, evaluated_at DESC);
CREATE INDEX idx_eval_result ON rule_evaluations (result_code);
CREATE INDEX idx_rules_enabled ON rules (enabled, rule_type);
CREATE INDEX idx_rules_conditions_gin ON rules USING GIN (conditions);
CREATE INDEX idx_scenarios_enabled ON scenarios (enabled);
CREATE INDEX idx_audit_target ON audit_logs (target_type, target_id, created_at DESC);
