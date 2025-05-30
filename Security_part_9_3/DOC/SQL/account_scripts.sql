CREATE TABLE IF NOT EXISTS accounts (
  account_id BIGSERIAL PRIMARY KEY,
  account_number BIGINT UNIQUE,
  client_id BIGINT REFERENCES clients (id),
  account_type varchar(128) NOT NULL,
  branch_address varchar(256) NOT NULL,
  create_dt date DEFAULT NULL
);

INSERT INTO accounts (account_id, account_number, client_id, account_type, branch_address, create_dt)
 VALUES (1, 186576453434, 1, 'Savings', '123 Main Street, New York', NOW()),
        (2, 386576453434, 2, 'Savings', '123 Main Street, New York', NOW()),
        (3, 273586453434, 3, 'Savings', '123 Main Street, New York', NOW()),
        (4, 009785453434, 4, 'Savings', '123 Main Street, New York', NOW()),
        (5, 853577453434, 5, 'Savings', '123 Main Street, New York', NOW());

SELECT SETVAL('accounts_account_id_seq', (SELECT MAX(account_id) FROM accounts));