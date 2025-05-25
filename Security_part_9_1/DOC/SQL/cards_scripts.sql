-- Создаем таблицу кредитных карт --
CREATE TABLE IF NOT EXISTS cards (
  card_id BIGSERIAL PRIMARY KEY,
  card_number varchar(128) NOT NULL,
  client_id BIGINT REFERENCES clients (id),
  card_type varchar(128) NOT NULL,
  total_limit int NOT NULL,
  amount_used int NOT NULL,
  available_amount int NOT NULL,
  create_dt date DEFAULT NULL
);

-- Заполняем таблицу кредитных карт --
INSERT INTO cards (card_id, card_number, client_id, card_type, total_limit, amount_used, available_amount, create_dt)
VALUES (1, '4565XXXX4656', 1, 'Credit', 10000, 500, 9500, NOW()),
       (2, '3455XXXX8673', 1, 'Credit', 7500, 600, 6900, NOW()),
       (3, '2359XXXX9346', 1, 'Credit', 20000, 4000, 16000, NOW()),
       (4, '3365XXXX4886', 2, 'Credit', 5000, 300, 8500, NOW()),
       (5, '5655XXXX8343', 2, 'Credit', 17500, 1600, 9900, NOW()),
       (6, '7859XXXX9322', 2, 'Credit', 2000, 3200, 6000, NOW());

-- Задаем (перемещаем) состояние ID счетчика таблицы cards --
SELECT SETVAL('cards_card_id_seq', (SELECT MAX(card_id) FROM cards));