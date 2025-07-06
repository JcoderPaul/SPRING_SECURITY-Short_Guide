--liquibase formatted sql

--changeset oldboy:1
SELECT SETVAL('cards_card_id_seq', (SELECT MAX(card_id) FROM cards));
/* liquibase rollback
empty
*/