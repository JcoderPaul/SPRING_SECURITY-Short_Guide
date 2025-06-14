--liquibase formatted sql

--changeset oldboy:1
SELECT SETVAL('clients_authorities_id_seq', (SELECT MAX(id) FROM clients_authorities));
/* liquibase rollback
empty
*/