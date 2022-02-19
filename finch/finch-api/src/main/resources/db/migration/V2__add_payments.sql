CREATE TABLE if NOT EXISTS payments
(
    id   SERIAL PRIMARY KEY,
    payment_id text,
    start_date date,
    end_date date,
    pay_date date,
    debit_date date,
    constraint payments_unique unique (payment_id)
);

CREATE TABLE if NOT EXISTS pay_statements
(
    id   SERIAL PRIMARY KEY,
    payment_id text,
    individual_id text,
    pay_type text,
    amount bigint,
    FOREIGN KEY (payment_id) REFERENCES payments (payment_id),
    constraint pay_statements_unique unique (payment_id, individual_id, pay_type)
);