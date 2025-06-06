--liquibase formatted sql

--changeset oldboy:1
INSERT INTO client_contacts (id, client_id, city, postal_code, address, building, apartment, home_phone, mobile_phone)
 VALUES (1, 1, 'Lovervill', '33425643', 'Roksburg st.', 132, 32, '555-5456-23-55', '+7-222-34-23456'),
        (2, 2, 'Gastings', '1277654', 'Oak av.', 2, 2, '342-222-123-213', '+7-435-2234-256'),
        (3, 3, 'Kasagava', '00987689', 'Takishiba st.', 223, 12, '2212-2212-434', '+7-008-2234-256'),
        (4, 4, 'Drogi', '223415', 'Dubki st.', 13, 7, '33-345456', '+7-1121-24-25'),
        (5, 5, 'Chigirin', '114489', 'Dmitro Svyatko st.', 43, 21, '231-3423-634-565', '+7-8877-234-846');
--rollback delete from client_contacts