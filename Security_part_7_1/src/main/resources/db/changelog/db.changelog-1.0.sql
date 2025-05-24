--liquibase formatted sql

--changeset oldboy:1
CREATE TABLE IF NOT EXISTS client_details (
  id BIGSERIAL PRIMARY KEY,
  client_name varchar(65) NOT NULL,
  client_surname varchar(65) NOT NULL,
  client_age int NOT NULL CONSTRAINT right_age CHECK (client_age > 0)
);
--rollback DROP TABLE client_details;