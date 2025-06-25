--liquibase formatted sql

--changeset oldboy:1
CREATE TABLE IF NOT EXISTS client_contacts (
  id BIGSERIAL PRIMARY KEY,
  client_id BIGINT REFERENCES clients (id),
  city varchar(65) NOT NULL,
  postal_code INT NOT NULL,
  address varchar(128) NOT NULL,
  building INT NOT NULL,
  apartment INT NOT NULL,
  home_phone varchar(32) NOT NULL UNIQUE,
  mobile_phone varchar(32) NOT NULL UNIQUE
);
--rollback DROP TABLE client_contacts;