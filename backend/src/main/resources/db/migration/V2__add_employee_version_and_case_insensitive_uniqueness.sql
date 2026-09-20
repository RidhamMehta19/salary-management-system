ALTER TABLE employees ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

UPDATE employees
SET employee_number = btrim(employee_number), email = lower(btrim(email));

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM employees GROUP BY lower(employee_number) HAVING count(*) > 1) THEN
        RAISE EXCEPTION 'Cannot normalize employee_number: case-insensitive duplicates exist';
    END IF;
    IF EXISTS (SELECT 1 FROM employees GROUP BY lower(email) HAVING count(*) > 1) THEN
        RAISE EXCEPTION 'Cannot normalize email: case-insensitive duplicates exist';
    END IF;
END $$;

ALTER TABLE employees DROP CONSTRAINT IF EXISTS employees_employee_number_key;
ALTER TABLE employees DROP CONSTRAINT IF EXISTS employees_email_key;
CREATE UNIQUE INDEX ux_employees_employee_number_lower ON employees (lower(employee_number));
CREATE UNIQUE INDEX ux_employees_email_lower ON employees (lower(email));
