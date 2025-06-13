--liquibase formatted sql

--changeset oldboy:1
SELECT SETVAL('accounts_account_id_seq', (SELECT MAX(account_id) FROM accounts));
/* liquibase rollback
empty
*/