--liquibase formatted sql

--changeset oldboy:1
INSERT INTO clients (id, email, pass, role, details_id)
VALUES (1, 'admin@test.com', '$2y$10$OZZhrg6W3oekdIgsLjtJJO.NmgTVBV6jGNWnN9UVD2OnbXvX8.4I2', 'ROLE_ADMIN', 1),
       (2, 'admin2@test.com', '$2y$10$Y7/d1fA/f6v0B1O5bYZzvutMgoYThaqCvg.J9O3DdwYRU70EP..WO', 'ROLE_ADMIN', 2),
       (3, 'user@test.com', '$2y$10$U1kgGBVqHFJ4AcnawvUdmeBxRE3593XZ4U0lEJCV2RWI/J3DW/f.G', 'ROLE_USER', 3),
       (4, 'user2@test.com', '$2y$10$H1TwhepYL0AyVUb3f3OCQupyDBiTL7DcWCgLfTqzFDXZl5JpsaU7a', 'ROLE_USER', 4),
       (5, 'user3@test.com', '$2y$10$YIKMHeHT0kCqB.PZHNDTF..PG3cFeymimZ48wGkxFeFMWmmE5dW46', 'ROLE_USER', 5);
--rollback delete from clients;