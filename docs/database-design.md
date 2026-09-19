# Database Design

PostgreSQL is the system of record. Flyway owns schema changes; Hibernate runs in `validate` mode so an accidental entity/schema mismatch fails at startup.

## Tables

| Table | Purpose | Important columns |
|---|---|---|
| `departments` | Controlled department reference data | `id`, unique `name`, timestamps |
| `employees` | Current employee and compensation record | unique `employee_number` and `email`, employee profile fields, `employment_status`, `currency`, `base_salary`, `bonus`, timestamps |
| `salary_history` | Append-only compensation snapshots | `employee_id`, salary values, `effective_date`, `change_reason`, `recorded_at` |

`employees.department_id` references `departments.id`. `salary_history.employee_id` references `employees.id`. Deactivation updates status and preserves both employee and history rows.

## Integrity and indexes

- Primary keys use UUIDs; business identifiers remain readable and unique.
- Required profile, status, currency, and salary fields are `NOT NULL`; salary values have non-negative checks.
- Name, department, country, and status indexes support the directory's common filters. The unique indexes on employee ID and email support fast duplicate checks.
- `salary_history(employee_id, effective_date DESC)` supports the detail page's newest-first history.
- Dashboard aggregates execute in PostgreSQL through grouped projections rather than loading 10,000 employee rows into the browser.

The seed profile creates the eight departments idempotently before inserting employees.
