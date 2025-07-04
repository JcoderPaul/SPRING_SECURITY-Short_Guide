--liquibase formatted sql

--changeset oldboy:1
CREATE TABLE IF NOT EXISTS cards (
  card_id BIGSERIAL PRIMARY KEY,
  card_number varchar(128) NOT NULL,
  client_id BIGINT REFERENCES clients (id),
  card_type varchar(128) NOT NULL,
  total_limit int NOT NULL,
  amount_used int NOT NULL,
  available_amount int NOT NULL,
  create_dt date DEFAULT NULL
);
--rollback DROP TABLE cards;