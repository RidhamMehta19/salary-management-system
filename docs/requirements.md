# Salary Management System — Requirements

## Goal

Replace spreadsheet-based salary administration with a focused web application that lets HR manage employee compensation records and understand how compensation is distributed across the organisation.

## Primary user

The primary persona is an **HR Manager** who maintains employee information, updates salary details, and uses aggregate insights to support compensation conversations. The system is sized for an organisation of approximately 10,000 employees.

## Problem

Excel files make it difficult to keep salary data consistent, discover changes over time, search for employees, and answer basic compensation questions without manual calculation. The product provides one relational source of truth with an accessible, browser-based workflow.

## In scope

- Employee directory with server-side search, filtering, sorting, and pagination.
- Employee records: employee ID, name, email, department, job title, country, location, employment status, hire date, and audit timestamps.
- Compensation records: currency, base salary, bonus, calculated total compensation, and an append-only salary-change history.
- HR workflows to add, edit, view, and deactivate employees (without deleting records).
- Dashboard metrics and meaningful breakdowns by department, country, headcount, and salary bands.
- PostgreSQL persistence, schema migrations, deterministic 10,000-employee seed profile, API validation/errors, responsive Angular UI, tests, and deployment guidance.

## Core features

1. **Employee management:** browse and find employees by name or ID; filter by department, country, and status; create, edit, inspect, and deactivate a record.
2. **Salary management:** display and update base salary, annual bonus, currency, and total compensation; retain prior compensation snapshots with an effective date and change reason.
3. **Salary insights:** show employee count, total compensation, average base salary, and concise department/country/distribution views based on persisted data.

## Out of scope

- Authentication, user roles, SSO, and audit identity: the assessment does not define access rules. The application is intended for a protected internal network/demo environment; production access control is a necessary next step.
- Payroll calculation, tax, benefits, payslips, disbursements, approval workflows, imports/exports, document storage, and multi-currency conversion.
- Employee self-service, deletion, bulk-editing, notifications, and custom report building.

## Assumptions and pragmatic defaults

- Salary and bonus are annual monetary amounts; total compensation is their sum in the stored currency. Dashboard totals do not convert currencies and are therefore labelled by currency where needed.
- Departments are centrally managed reference data; country and location are stored directly on an employee because no country-specific configuration is required.
- “Deactivation” preserves the record and salary history, sets an inactive status, and excludes the employee from active-workforce metrics.
- A single current compensation record is stored with each employee; each create or update produces a salary history snapshot.
- The initial data set is generated deterministically only when the `seed` profile is enabled and the employee table is empty.

## Success criteria

An HR Manager can efficiently manage and find salary records for 10,000 seeded employees, see trustworthy dashboard answers derived from PostgreSQL, and use the responsive UI without browser-side bulk loading. The application validates invalid data, reports actionable errors, keeps salary history, passes meaningful automated tests, and is reproducible locally and deployable with environment-based configuration.
