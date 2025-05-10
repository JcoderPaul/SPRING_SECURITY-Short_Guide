CREATE TABLE IF NOT EXISTS users(
  id BIGSERIAL PRIMARY KEY ,
  username varchar(65) unique,
  password varchar(100),
  enabled int
);

CREATE TABLE IF NOT EXISTS authorities(
  id BIGSERIAL PRIMARY KEY ,
  username varchar(65),
  authority varchar(25),
  FOREIGN KEY (username) references users(username)
);

INSERT INTO users (username, password, enabled)
VALUES ('Paul', '{noop}12345', 1),
       ('Sasha', '{noop}54321', 1),
       ('Stasya', '{noop}98765', 1);

INSERT INTO authorities (username, authority)
VALUES ('Paul', 'ROLE_EMPLOYEE'),
       ('Sasha', 'ROLE_HR'),
       ('Stasya', 'ROLE_HR'),
       ('Stasya', 'ROLE_MANAGER');