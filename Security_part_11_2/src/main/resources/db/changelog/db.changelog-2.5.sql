--liquibase formatted sql

--changeset oldboy:1
CREATE TABLE IF NOT EXISTS clients_authorities (
  id BIGSERIAL PRIMARY KEY,
  client_id BIGINT REFERENCES clients (id) NOT NULL,
  authority_id BIGINT REFERENCES authorities (id) NOT NULL,
  UNIQUE (client_id, authority_id)
);
--rollback DROP TABLE clients_authorities;