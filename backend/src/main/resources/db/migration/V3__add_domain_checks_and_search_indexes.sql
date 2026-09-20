ALTER TABLE employees
    ADD CONSTRAINT ck_employees_employment_status CHECK (employment_status IN ('ACTIVE', 'INACTIVE', 'ON_LEAVE')),
    ADD CONSTRAINT ck_employees_currency CHECK (currency IN ('USD', 'INR', 'GBP', 'EUR', 'CAD', 'AUD'));

CREATE EXTENSION IF NOT EXISTS pg_trgm;
-- Trigram indexes provide the search path needed for the 1M-row employee dataset.
CREATE INDEX ix_employees_first_name_lower_trgm ON employees USING gin (lower(first_name) gin_trgm_ops);
CREATE INDEX ix_employees_last_name_lower_trgm ON employees USING gin (lower(last_name) gin_trgm_ops);
CREATE INDEX ix_employees_number_lower_trgm ON employees USING gin (lower(employee_number) gin_trgm_ops);
