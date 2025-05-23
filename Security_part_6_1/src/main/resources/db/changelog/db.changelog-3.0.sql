--liquibase formatted sql

--changeset oldboy:1
INSERT INTO client_details (id, client_name, client_surname, client_age)
VALUES (1, 'Malkolm', 'Stone', 19),
       (2, 'Snara', 'Kuesta', 17),
       (3, 'Duglas', 'Lind', 18),
       (4, 'Timus', 'Rodrik', 123),
       (5, 'Shiban', 'Taru', 17);
--rollback delete from client_details