--liquibase formatted sql

--changeset oldboy:1
CREATE TABLE IF NOT EXISTS account_transactions (
  transaction_id BIGSERIAL PRIMARY KEY,
  account_number BIGINT REFERENCES accounts (account_number),
  client_id BIGINT REFERENCES clients (id),
  transaction_dt date NOT NULL,
  transaction_summary varchar(256) NOT NULL,
  transaction_type varchar(128) NOT NULL,
  transaction_amt int NOT NULL,
  closing_balance int NOT NULL,
  create_dt date DEFAULT NULL
);
--rollback DROP TABLE account_transactions;