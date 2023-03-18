CREATE DATABASE bank;

CREATE TABLE balance (customer_id SERIAL, value INTEGER);

CREATE TABLE customer (id SERIAL, name VARCHAR(255),cpf VARCHAR(100),celphone VARCHAR(100));