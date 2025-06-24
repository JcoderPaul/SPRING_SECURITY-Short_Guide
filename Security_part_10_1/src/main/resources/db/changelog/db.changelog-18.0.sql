--liquibase formatted sql

--changeset oldboy:1
SELECT SETVAL('loans_loan_id_seq', (SELECT MAX(loan_id) FROM loans));
/* liquibase rollback
empty
*/