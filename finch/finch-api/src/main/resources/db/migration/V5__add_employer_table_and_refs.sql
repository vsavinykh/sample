ALTER TABLE finch_access_token RENAME COLUMN employer_id TO old_employer_id;
ALTER TABLE finch_access_token ADD COLUMN employer_id BIGINT;

ALTER TABLE finch_request_token RENAME COLUMN employer_id TO old_employer_id;
ALTER TABLE finch_request_token ADD COLUMN employer_id BIGINT;

ALTER TABLE lh_employee ADD COLUMN employer_id BIGINT;

ALTER TABLE payments RENAME COLUMN employer_id TO old_employer_id;
ALTER TABLE payments ADD COLUMN employer_id BIGINT;

CREATE TABLE if NOT EXISTS employer
(
    id                  BIGSERIAL PRIMARY KEY,
    lh_employer_id      TEXT UNIQUE,
    deduction_id        TEXT
);

INSERT INTO employer (lh_employer_id)
SELECT old_employer_id FROM finch_access_token
GROUP BY old_employer_id;

INSERT INTO employer (lh_employer_id)
SELECT old_employer_id FROM finch_request_token
GROUP BY old_employer_id
ON CONFLICT ON CONSTRAINT employer_lh_employer_id_key
DO NOTHING;

INSERT INTO employer (lh_employer_id)
SELECT lane_employer_id FROM lh_employee
GROUP BY lane_employer_id
ON CONFLICT ON CONSTRAINT employer_lh_employer_id_key
DO NOTHING;

INSERT INTO employer (lh_employer_id)
SELECT old_employer_id FROM payments
GROUP BY old_employer_id
ON CONFLICT ON CONSTRAINT employer_lh_employer_id_key
DO NOTHING;

UPDATE finch_access_token
SET employer_id = e.id
FROM employer e
WHERE finch_access_token.old_employer_id = e.lh_employer_id;

ALTER TABLE finch_access_token
DROP COLUMN IF EXISTS old_employer_id;

ALTER TABLE finch_access_token
ADD CONSTRAINT fk_finch_access_token_employer
FOREIGN KEY (employer_id) REFERENCES employer (id);

UPDATE finch_request_token
SET employer_id = e.id
FROM employer e
WHERE finch_request_token.old_employer_id = e.lh_employer_id;

ALTER TABLE finch_request_token
DROP COLUMN IF EXISTS old_employer_id;

ALTER TABLE finch_request_token
ADD CONSTRAINT fk_finch_request_token_employer
FOREIGN KEY (employer_id) REFERENCES employer (id);

UPDATE lh_employee
SET employer_id = e.id
FROM employer e
WHERE lh_employee.lane_employer_id = e.lh_employer_id;

ALTER TABLE lh_employee
DROP COLUMN IF EXISTS lane_employer_id;

ALTER TABLE lh_employee
ADD CONSTRAINT fk_lh_employee_employer
FOREIGN KEY (employer_id) REFERENCES employer (id);

UPDATE payments
SET employer_id = e.id
FROM employer e
WHERE payments.old_employer_id = e.lh_employer_id;

ALTER TABLE payments
DROP COLUMN IF EXISTS old_employer_id;

ALTER TABLE payments
ADD CONSTRAINT fk_payments_employer
FOREIGN KEY (employer_id) REFERENCES employer (id);