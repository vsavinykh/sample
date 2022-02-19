ALTER TABLE lh_employee ADD UNIQUE (lane_employee_id);
ALTER TABLE lh_employee ADD COLUMN IF NOT EXISTS payroll_id BIGINT;
ALTER TABLE employer ADD UNIQUE (deduction_id);

CREATE TABLE if NOT EXISTS sent_deduction
(
    id   SERIAL PRIMARY KEY,
    lh_employee_id bigint,
    deduction_id text,
    employer_id text,
    amount bigint,
    sent_at timestamp,
    pay_date date,
    payroll_id BIGINT,
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    is_sent_to_finch boolean,
    FOREIGN KEY (lh_employee_id) REFERENCES lh_employee (lane_employee_id),
    FOREIGN KEY (deduction_id) REFERENCES employer (deduction_id),
    constraint sent_deduction_unique unique (lh_employee_id, payroll_id, pay_date)
);