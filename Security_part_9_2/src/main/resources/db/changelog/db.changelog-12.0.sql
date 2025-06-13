--liquibase formatted sql

--changeset oldboy:1
CREATE TABLE IF NOT EXISTS loans (
  loan_id BIGSERIAL PRIMARY KEY,
  client_id BIGINT REFERENCES clients (id),
  start_dt date NOT NULL,
  loan_type varchar(128) NOT NULL,
  total_loan int NOT NULL,
  amount_paid int NOT NULL,
  outstanding_amount int NOT NULL,
  create_dt date DEFAULT NULL
);
--rollback DROP TABLE loans;