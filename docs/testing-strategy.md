# Testing Strategy

## What is tested

- `EmployeeServiceTest` checks creation, initial salary history, compensation and currency-only changes, same-day effective dates through an injected clock, non-compensation edits, pagination metadata, and missing employee behavior.
- `DashboardServiceTest` checks active headcount and currency aggregate mapping.
- `EmployeeControllerTest` checks validation and safe 404 responses.
- `EmployeeApiIntegrationTest` starts the full Spring context against Testcontainers PostgreSQL, runs the production Flyway migrations, and verifies optimistic locking, case-insensitive DB uniqueness, deterministic pagination, invalid paging/sorting, unknown departments, date/currency validation, migration checks, create/search/filter, status change, history retrieval, malformed/invalid input, 404, duplicate conflicts, and currency-specific salary bands.

Run with `mvn -f backend/pom.xml test` while Docker is available. Without Docker, Testcontainers fails clearly rather than silently changing database behavior.
- Angular tests verify API query parameter construction, employee filter reset/pagination behavior, dashboard success/failure states, employee detail loading/status transitions/in-flight disabling, form create/update/conflict flows and validation, department-load failure, initials, status labels, and safe save-error messages.

## Why this level

The tests target business rules, persistence wiring, and user-visible API contracts rather than getters or framework behavior. The integration test catches migration/entity mismatches that unit tests cannot.

## Intentionally not tested

The assessment does not require visual snapshot tests, browser-level end-to-end tests against a deployed host, authentication, payroll rules, currency conversion, or 10,000-row performance benchmarking. Those need stable product/security requirements and an environment beyond the core take-home flow.

Run backend tests with `mvn -f backend/pom.xml test`; this repository currently does not include an `mvnw` wrapper. Run frontend tests with `npm --prefix frontend test -- --watch=false --browsers=ChromeHeadless`.
