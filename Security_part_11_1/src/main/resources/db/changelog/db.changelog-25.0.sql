--liquibase formatted sql

--changeset oldboy:1
INSERT INTO account_transactions (transaction_id, account_number, client_id, transaction_dt, transaction_summary, transaction_type, transaction_amt, closing_balance, create_dt)
VALUES (1, 186576453434, 1, NOW()-interval '7 day', 'Coffee Shop', 'Withdrawal', 30, 34500, NOW()-interval '7 day'),
       (2, 186576453434, 1, NOW()-interval '6 day', 'Uber', 'Withdrawal', 100, 34400, NOW()-interval '6 day'),
       (3, 186576453434, 1, NOW()-interval '5 day', 'Self Deposit', 'Deposit', 500, 34900, NOW()-interval '5 day'),
       (4, 186576453434, 1, NOW()-interval '4 day', 'Ebay', 'Withdrawal', 600, 34300, NOW()-interval '4 day'),
       (5, 186576453434, 1, NOW()-interval '2 day', 'OnlineTransfer', 'Deposit', 700, 35000, NOW()-interval '2 day'),
       (6, 186576453434, 1, NOW()-interval '1 day', 'Amazon.com', 'Withdrawal', 100, 34900, NOW()-interval '1 day');
--rollback delete from account_transactions