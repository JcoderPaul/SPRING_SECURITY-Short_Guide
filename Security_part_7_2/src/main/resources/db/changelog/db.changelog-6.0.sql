--liquibase formatted sql

--changeset oldboy:1
SELECT SETVAL('clients_id_seq', (SELECT MAX(id) FROM clients));
/* liquibase rollback
empty
*/