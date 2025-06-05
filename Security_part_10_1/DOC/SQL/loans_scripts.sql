-- Создаем таблицу связывающую клиентов и их кредиты --
CREATE TABLE IF NOT EXISTS loans (
  loan_id BIGSERIAL PRIMARY KEY,
  client_id BIGINT REFERENCES clients (id),
  start_dt date NOT NULL,
  loan_type varchar(128) NOT NULL,
  total_loan int NOT NULL,
  amount_paid int NOT NULL,
  outstanding_amount int NOT NULL,
  create_dt date DEFAULT NULL
);

-- Заполняем таблицу клиентских кредитов --
INSERT INTO loans (loan_id, client_id, start_dt, loan_type, total_loan, amount_paid, outstanding_amount, create_dt)
 VALUES (1, 1, '2020-10-13', 'Home', 200000, 50000, 150000, '2020-10-13'),
        (2, 1, '2020-06-06', 'Vehicle', 40000, 10000, 30000, '2020-06-06'),
        (3, 1, '2018-02-14', 'Home', 50000, 10000, 40000, '2018-02-14'),
        (4, 1, '2018-02-14', 'Personal', 10000, 3500, 6500, '2018-02-14'),
        (5, 2, '2020-10-13', 'Cottage', 400000, 30000, 100000, '2020-10-13'),
        (6, 2, '2020-06-06', 'Motorcycle', 20000, 5000, 10000, '2020-06-06'),
        (7, 2, '2018-02-14', 'Furniture', 5000, 1000, 4000, '2018-02-14'),
        (8, 2, '2018-02-14', 'Personal', 11000, 4200, 7300, '2018-02-14');

-- Задаем (перемещаем) состояние ID счетчика таблицы loans --
SELECT SETVAL('loans_loan_id_seq', (SELECT MAX(loan_id) FROM loans));