create database if not exists finance_db;

CREATE TABLE IF NOT EXISTS finance_db.accounts (
    account_id STRING,
    customer_id STRING,
    account_type STRING,
    open_date STRING,
    close_date STRING,
    status STRING,
    current_balance DOUBLE
)
PARTITIONED BY (business_date STRING)
STORED AS PARQUET
TBLPROPERTIES (
    'parquet.compression' = 'SNAPPY'
);
