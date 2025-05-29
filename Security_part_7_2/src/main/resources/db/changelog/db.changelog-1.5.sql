--liquibase formatted sql

--changeset oldboy:1
CREATE TABLE IF NOT EXISTS authorities (
  id BIGSERIAL PRIMARY KEY,
  authority_name varchar(50) NOT NULL UNIQUE
);
--rollback DROP TABLE authorities;