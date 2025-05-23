--liquibase formatted sql

--changeset oldboy:1
CREATE TABLE IF NOT EXISTS persistent_logins (
  username  VARCHAR(64) NOT NULL,
  series    VARCHAR(64) NOT NULL,
  token     VARCHAR(64) NOT NULL,
  last_used TIMESTAMP   NOT NULL,
  PRIMARY KEY (series)
);
--rollback DROP TABLE client_details;