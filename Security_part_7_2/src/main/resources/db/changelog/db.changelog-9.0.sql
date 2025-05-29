--liquibase formatted sql

--changeset oldboy:1
SELECT SETVAL('authorities_id_seq', (SELECT MAX(id) FROM authorities));
/* liquibase rollback
empty
*/