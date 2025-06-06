--liquibase formatted sql

--changeset oldboy:1
INSERT INTO accounts (account_id, account_number, client_id, account_type, branch_address, create_dt)
 VALUES (1, 186576453434, 1, 'Savings', '123 Main Street, New York', NOW()),
        (2, 386576453434, 2, 'Savings', '123 Main Street, New York', NOW()),
        (3, 273586453434, 3, 'Savings', '123 Main Street, New York', NOW()),
        (4, 009785453434, 4, 'Savings', '123 Main Street, New York', NOW()),
        (5, 853577453434, 5, 'Savings', '123 Main Street, New York', NOW());
--rollback delete from accounts