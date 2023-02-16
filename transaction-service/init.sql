CREATE DATABASE bank;

CREATE TABLE transaction (
    customer_id INTEGER,
    type VARCHAR(255),
    value INTEGER,
    description VARCHAR(255)
);

CREATE TABLE customer (id INTEGER, name VARCHAR(255));