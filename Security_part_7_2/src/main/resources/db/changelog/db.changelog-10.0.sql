--liquibase formatted sql

--changeset oldboy:1
INSERT INTO clients_authorities (id, client_id, authority_id)
VALUES (1, 1, 5),
       (2, 2, 2),
       (3, 2, 3),
       (4, 3, 1),
       (5, 3, 4);
--rollback delete from clients_authorities