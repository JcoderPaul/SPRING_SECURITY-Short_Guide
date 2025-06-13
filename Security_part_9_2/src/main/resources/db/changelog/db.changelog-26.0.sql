--liquibase formatted sql

--changeset oldboy:1
SELECT SETVAL('account_transactions_transaction_id_seq', (SELECT MAX(transaction_id) FROM account_transactions));
/* liquibase rollback
empty
*/