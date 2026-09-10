-- 예비군 동원소집 통합 관제 시스템 - 초기 스키마
-- docs/db-design.md 기준 (DBMS: PostgreSQL 16)

CREATE TABLE unit (
    unit_id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    unit_name           VARCHAR(100) NOT NULL,
    location            VARCHAR(200) NOT NULL,
    entry_deadline_time TIME         NOT NULL,
    contact             VARCHAR(50)
);

CREATE TABLE reservist (
    reservist_id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    display_name           VARCHAR(50)   NOT NULL,
    residence_distance_km  NUMERIC(6,1)  NOT NULL,
    unit_id                BIGINT        NOT NULL REFERENCES unit (unit_id),
    status                 VARCHAR(20)   NOT NULL DEFAULT '소집전'
        CHECK (status IN ('소집전', '입영', '지연', '조기퇴소', '완료'))
);

CREATE TABLE user_account (
    user_id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    login_id       VARCHAR(50)  NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    role           VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'RESERVIST')),
    unit_id        BIGINT REFERENCES unit (unit_id),
    reservist_id   BIGINT REFERENCES reservist (reservist_id),
    created_at     TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE callup_notice (
    notice_id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reservist_id        BIGINT NOT NULL REFERENCES reservist (reservist_id),
    unit_id             BIGINT NOT NULL REFERENCES unit (unit_id),
    scheduled_datetime  TIMESTAMP NOT NULL,
    notice_status       VARCHAR(20) NOT NULL DEFAULT '발송완료'
);

CREATE TABLE entry_record (
    entry_id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reservist_id             BIGINT NOT NULL REFERENCES reservist (reservist_id),
    actual_entry_datetime    TIMESTAMP,
    entry_status             VARCHAR(20) NOT NULL DEFAULT '대기'
        CHECK (entry_status IN ('대기', '완료', '지연', '미입영'))
);

CREATE TABLE judgment_result (
    judgment_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reservist_id    BIGINT NOT NULL REFERENCES reservist (reservist_id),
    judgment_type   VARCHAR(20) NOT NULL CHECK (judgment_type IN ('지연입소', '조기퇴소')),
    is_auto         BOOLEAN NOT NULL DEFAULT true,
    result          VARCHAR(10) NOT NULL CHECK (result IN ('허용', '불허')),
    reason          VARCHAR(255),
    judged_at       TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE process_log (
    log_id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    judgment_id   BIGINT NOT NULL REFERENCES judgment_result (judgment_id),
    user_id       BIGINT NOT NULL REFERENCES user_account (user_id),
    processed_at  TIMESTAMP NOT NULL DEFAULT now(),
    content       VARCHAR(255) NOT NULL
);

-- 인덱스 설계 (docs/db-design.md 5절)
CREATE INDEX idx_reservist_unit_status ON reservist (unit_id, status);
CREATE INDEX idx_callup_notice_reservist ON callup_notice (reservist_id);
CREATE INDEX idx_entry_record_reservist_status ON entry_record (reservist_id, entry_status);
CREATE INDEX idx_judgment_result_reservist_type ON judgment_result (reservist_id, judgment_type);
CREATE INDEX idx_process_log_judgment ON process_log (judgment_id);
