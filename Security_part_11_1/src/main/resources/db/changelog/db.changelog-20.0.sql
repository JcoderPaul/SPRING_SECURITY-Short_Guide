--liquibase formatted sql

--changeset oldboy:1
SELECT SETVAL('client_contacts_id_seq', (SELECT MAX(id) FROM client_contacts));
/* liquibase rollback
empty
*/