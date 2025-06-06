--liquibase formatted sql

--changeset oldboy:1
SELECT SETVAL('client_details_id_seq', (SELECT MAX(id) FROM client_details));
/* liquibase rollback
empty
*/