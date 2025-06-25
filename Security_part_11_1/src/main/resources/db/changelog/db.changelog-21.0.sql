--liquibase formatted sql

--changeset oldboy:1
INSERT INTO cards (card_id, card_number, client_id, card_type, total_limit, amount_used, available_amount, create_dt)
VALUES (1, '4565XXXX4656', 1, 'Credit', 10000, 500, 9500, NOW()),
       (2, '3455XXXX8673', 1, 'Credit', 7500, 600, 6900, NOW()),
       (3, '2359XXXX9346', 1, 'Credit', 20000, 4000, 16000, NOW()),
       (4, '3365XXXX4886', 3, 'Credit', 5000, 300, 8500, NOW()),
       (5, '5655XXXX8343', 3, 'Credit', 17500, 1600, 9900, NOW()),
       (6, '7859XXXX9322', 3, 'Credit', 2000, 3200, 6000, NOW());
--rollback delete from cards