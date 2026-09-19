# Final Review

## Implemented

- Requirements-first scope with documented assumptions and exclusions.
- Java/Spring Boot REST API using DTOs, validation, centralized errors, Flyway, JPA repositories, indexed PostgreSQL schema, and health endpoint.
- Employee CRUD-like workflow with non-destructive status changes and salary-history snapshots.
- Server-side directory search/filter/sort/pagination suitable for 10,000 records.
- Dashboard aggregates from persisted data, grouped by currency where conversion is unspecified.
- Fully deterministic seeded dataset, Angular/PrimeNG responsive UI, loading/empty/error states, confirmation, and success feedback.
- Backend unit/API integration tests and frontend ChromeHeadless tests (locally verified 2026-09-20).
- Docker, environment examples, deployment instructions, architecture/design/testing/AI artifacts, and incremental Git history.

## Intentionally excluded

Authentication and authorization, SSO, audit identity, payroll/tax/benefits, currency conversion, bulk import/export, deletion, notifications, and custom reporting remain outside this assessment's defined requirements.

## Known limitations and technical debt

- The current frontend runtime config uses a safe same-origin `/api` default; the hosting deployment must replace `app-config.js` with its deployed API URL when frontend and backend are on different origins.
- Dashboard values are not exchange-rate normalized and must be read with their currency labels.
- No browser end-to-end test runs against a deployed provider; the API integration test and component/service tests cover the core behavior.
- The directory currently has no debounced search-on-type; Enter or filter changes trigger a request.
- Authentication, audit actor metadata, rate limiting, and production data retention policies are required before real salary data is used.

## Future improvements

1. Add SSO/RBAC and actor-aware audit logs.
2. Add a reviewed CSV import with dry-run validation and background progress.
3. Add exchange-rate snapshots only if business owners require comparable cross-currency reporting.
4. Add Playwright smoke tests against a deployed preview and database query-plan monitoring.

## Interview discussion points

- Why a modular monolith and PostgreSQL are proportionate to this workload.
- Why pagination and aggregate projections protect the UI and database access pattern.
- Why salary history is append-only and compensation updates are the only events recorded.
- Why deactivation preserves data and changes active metrics.
- Why currency-grouped totals are more truthful than an invented conversion rate.
- How the seed profile, Flyway validation, integration test, and environment configuration make local and cloud deployments reproducible.
- How the AI assistant was used for acceleration while tests, compiler diagnostics, and design review constrained the output.
