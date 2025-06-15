create database if not exists finance_db;

CREATE TABLE IF NOT EXISTS finance_db.customers (
    customer_id STRING,
    full_name STRING,
    date_of_birth STRING,
    gender STRING,
    nationality STRING,
    government_id STRING,
    ssn STRING,
    marital_status STRING,
    home_address STRING,
    mailing_address STRING,
    email STRING,
    phone_numbers ARRAY<STRING>
)
PARTITIONED BY (business_date STRING)
STORED AS PARQUET
TBLPROPERTIES (
    'parquet.compression' = 'SNAPPY'
);
