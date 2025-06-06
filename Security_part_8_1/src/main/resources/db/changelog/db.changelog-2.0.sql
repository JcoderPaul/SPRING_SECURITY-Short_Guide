--liquibase formatted sql

--changeset oldboy:1
CREATE TABLE IF NOT EXISTS clients (
  id BIGSERIAL PRIMARY KEY,
  email varchar(65) NOT NULL UNIQUE,
  pass varchar(255) NOT NULL,
  role varchar(65) NOT NULL,
  details_id BIGINT REFERENCES client_details (id)
);
--rollback DROP TABLE clients;