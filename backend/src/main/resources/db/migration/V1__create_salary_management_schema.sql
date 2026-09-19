CREATE TABLE departments (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE employees (
    id UUID PRIMARY KEY,
    employee_number VARCHAR(30) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    department_id UUID NOT NULL REFERENCES departments(id),
    job_title VARCHAR(150) NOT NULL,
    country VARCHAR(100) NOT NULL,
    location VARCHAR(150) NOT NULL,
    employment_status VARCHAR(20) NOT NULL,
    hire_date DATE NOT NULL,
    currency VARCHAR(3) NOT NULL,
    base_salary NUMERIC(14, 2) NOT NULL CHECK (base_salary >= 0),
    bonus NUMERIC(14, 2) NOT NULL CHECK (bonus >= 0),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE salary_history (
    id UUID PRIMARY KEY,
    employee_id UUID NOT NULL REFERENCES employees(id),
    currency VARCHAR(3) NOT NULL,
    base_salary NUMERIC(14, 2) NOT NULL CHECK (base_salary >= 0),
    bonus NUMERIC(14, 2) NOT NULL CHECK (bonus >= 0),
    effective_date DATE NOT NULL,
    change_reason VARCHAR(500) NOT NULL,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_employee_name ON employees(last_name, first_name);
CREATE INDEX idx_employee_department ON employees(department_id);
CREATE INDEX idx_employee_country ON employees(country);
CREATE INDEX idx_employee_status ON employees(employment_status);
CREATE INDEX idx_salary_history_employee_effective ON salary_history(employee_id, effective_date DESC);
