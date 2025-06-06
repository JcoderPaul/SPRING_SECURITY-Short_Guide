--liquibase formatted sql

--changeset oldboy:1
CREATE TABLE IF NOT EXISTS accounts (
  account_id BIGSERIAL PRIMARY KEY,
  account_number BIGINT UNIQUE,
  client_id BIGINT REFERENCES clients (id),
  account_type varchar(128) NOT NULL,
  branch_address varchar(256) NOT NULL,
  create_dt date DEFAULT NULL
);
--rollback DROP TABLE accounts;