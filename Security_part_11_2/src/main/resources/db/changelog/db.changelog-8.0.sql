--liquibase formatted sql

--changeset oldboy:1
INSERT INTO authorities (id, authority_name)
VALUES (1, 'READ'),
       (2, 'WRITE'),
       (3, 'DELETE'),
       (4, 'EDIT'),
       (5, 'MORE BAD ACTION');
--rollback delete from authorities