-- Создаем таблицу клиентов --
CREATE TABLE IF NOT EXISTS clients (
  id BIGSERIAL PRIMARY KEY,
  email varchar(65) NOT NULL UNIQUE,
  pass varchar(255) NOT NULL,
  role varchar(65) NOT NULL,
  details_id BIGINT REFERENCES client_details (id)
);

-- Заполняем таблицу клиентов - пароль у всех клиентов 1234 до Bcrypt шифрования --
INSERT INTO clients (id, email, pass, role, details_id)
VALUES (1, 'admin@test.com', '$2y$10$OZZhrg6W3oekdIgsLjtJJO.NmgTVBV6jGNWnN9UVD2OnbXvX8.4I2', 'ADMIN', 1),
       (2, 'admin2@test.com', '$2y$10$Y7/d1fA/f6v0B1O5bYZzvutMgoYThaqCvg.J9O3DdwYRU70EP..WO', 'ADMIN', 2),
       (3, 'user@test.com', '$2y$10$U1kgGBVqHFJ4AcnawvUdmeBxRE3593XZ4U0lEJCV2RWI/J3DW/f.G', 'USER', 3),
       (4, 'user2@test.com', '$2y$10$H1TwhepYL0AyVUb3f3OCQupyDBiTL7DcWCgLfTqzFDXZl5JpsaU7a', 'USER', 4),
       (5, 'user3@test.com', '$2y$10$YIKMHeHT0kCqB.PZHNDTF..PG3cFeymimZ48wGkxFeFMWmmE5dW46', 'USER', 5);

-- Задаем (перемещаем) состояние ID счетчика таблицы clients --
SELECT SETVAL('clients_id_seq', (SELECT MAX(id) FROM clients));

-- Создаем таблицу расширенных данных на клиентов --
CREATE TABLE IF NOT EXISTS client_details (
  id BIGSERIAL PRIMARY KEY,
  client_name varchar(65) NOT NULL,
  client_surname varchar(65) NOT NULL,
  client_age int NOT NULL CONSTRAINT right_age CHECK (client_age > 0)
);

-- Заполняем таблицу расширенных данных на клиентов --
INSERT INTO client_details (id, client_name, client_surname, client_age)
VALUES (1, 'Malkolm', 'Stone', 19),
       (2, 'Snara', 'Kuesta', 17),
       (3, 'Duglas', 'Lind', 18),
       (4, 'Timus', 'Rodrik', 123),
       (5, 'Shiban', 'Taru', 17);

-- Задаем (перемещаем) состояние ID счетчика таблицы client_details --
SELECT SETVAL('client_details_id_seq', (SELECT MAX(id) FROM client_details));