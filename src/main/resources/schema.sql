-- ============================================================
-- SCHEMA: account-transaction-backend
-- Execute this in Oracle SQL Developer before starting the app.
-- All tables use SEQUENCES (START WITH 11) so JPA inserts
-- (starting at ID 11) never collide with manually seeded rows 1–10.
-- ============================================================

CREATE SEQUENCE customer_seq START WITH 11 INCREMENT BY 1;
CREATE SEQUENCE account_seq START WITH 11 INCREMENT BY 1;
CREATE SEQUENCE transaction_seq START WITH 11 INCREMENT BY 1;
CREATE SEQUENCE idempotency_seq START WITH 11 INCREMENT BY 1;

-- ============================================================
-- TABLE: customer
-- ============================================================
CREATE TABLE customer (
    id              NUMBER PRIMARY KEY,
    first_name      VARCHAR2(100)   NOT NULL,
    last_name       VARCHAR2(100)   NOT NULL,
    email           VARCHAR2(255)   NOT NULL UNIQUE,
    phone_number    VARCHAR2(20),
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- ============================================================
-- TABLE: account
-- ============================================================
CREATE TABLE account (
    id              NUMBER PRIMARY KEY,
    account_number  VARCHAR2(36)    NOT NULL UNIQUE,
    customer_id     NUMBER          NOT NULL,
    balance         NUMBER(15, 2)   DEFAULT 0.00 NOT NULL,
    status          VARCHAR2(20)    DEFAULT 'ACTIVE' NOT NULL,
    version         NUMBER(10)      DEFAULT 0 NOT NULL,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_acc_cust  FOREIGN KEY (customer_id) REFERENCES customer(id),
    CONSTRAINT chk_balance  CHECK (balance >= 0),
    CONSTRAINT chk_status   CHECK (status IN ('ACTIVE', 'FROZEN', 'INACTIVE'))
);

-- ============================================================
-- TABLE: transaction
-- ============================================================
CREATE TABLE transaction (
    id                  NUMBER PRIMARY KEY,
    transaction_ref     VARCHAR2(36)    NOT NULL UNIQUE,
    account_id          NUMBER          NOT NULL,
    type                VARCHAR2(20)    NOT NULL,
    amount              NUMBER(15, 2)   NOT NULL,
    balance_before      NUMBER(15, 2)   NOT NULL,
    balance_after       NUMBER(15, 2)   NOT NULL,
    reference_id        VARCHAR2(36),
    description         VARCHAR2(255),
    transaction_date    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_txn_account FOREIGN KEY (account_id) REFERENCES account(id),
    CONSTRAINT chk_amount     CHECK (amount > 0),
    CONSTRAINT chk_type       CHECK (type IN ('DEPOSIT', 'WITHDRAW', 'TRANSFER_IN', 'TRANSFER_OUT'))
);

CREATE INDEX idx_txn_account_date ON transaction(account_id, transaction_date);

-- ============================================================
-- TABLE: idempotency_record
-- ============================================================
CREATE TABLE idempotency_record (
    id              NUMBER PRIMARY KEY,
    idempotency_key VARCHAR2(36)    NOT NULL UNIQUE,
    response_body   CLOB            NOT NULL,
    http_status     NUMBER(3)       NOT NULL,
    created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP NOT NULL
);
