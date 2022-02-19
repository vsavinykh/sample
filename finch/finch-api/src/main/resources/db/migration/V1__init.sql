CREATE TABLE if NOT EXISTS finch_employee
(
    id   SERIAL PRIMARY KEY,
    first_name text,
    middle_name text,
    last_name text,
    date_of_birthday date,
    ssn text,
    individual_id text
);

CREATE TABLE if NOT EXISTS lh_employee
(
    id   SERIAL PRIMARY KEY,
    first_name text,
    middle_name text,
    last_name text,
    date_of_birthday date,
    ssn text,
    lane_employee_id bigint,
    division text,
    lane_employer_id text,
    finch_employee_id integer,
    FOREIGN KEY (finch_employee_id) REFERENCES finch_employee (id),
    constraint lane_employee_id_unique_lane_employer_id unique (lane_employee_id, lane_employer_id)
);

CREATE TABLE if NOT EXISTS finch_access_token
(
    id                          BIGSERIAL PRIMARY KEY,
    employer_id                 TEXT,
    finch_access_token          TEXT,
    created_at                  TIMESTAMP NOT NULL DEFAULT now(),
    updated_at                  TIMESTAMP NOT NULL DEFAULT now(),
    is_active                   BOOL
);

CREATE TABLE if NOT EXISTS finch_request_token
(
    id                          BIGSERIAL PRIMARY KEY,
    employer_id                 TEXT,
    issuer_id                   TEXT,
    request_token               VARCHAR(50),
    created_at                  TIMESTAMP NOT NULL DEFAULT now(),
    updated_at                  TIMESTAMP NOT NULL DEFAULT now(),
    expires_on                  TIMESTAMP NOT NULL,
    status                      VARCHAR(10)
);