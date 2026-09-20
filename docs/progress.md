# Implementation Progress

This file is the working checkpoint for the Incubyte salary-management assessment. It records the requested scope, implementation stages, validation gates, and remaining delivery work. It is updated as the repository evolves.

## Assessment deliverables

- [x] Monorepo initialized with `backend/`, `frontend/`, and `docs/`.
- [x] Incremental Git history started with initialization and requirements commits.
- [x] One-page product requirements document created before application code.
- [x] Backend implementation completed and documented.
- [x] Angular/PrimeNG UI implementation completed and documented.
- [x] PostgreSQL schema and migrations completed and documented.
- [x] Deterministic 10,000-employee seed completed and documented.
- [x] Backend unit tests verified: 10 passing, 0 failing (2026-09-20); API integration tests remain Docker-dependent.
- [x] Local Docker workflow and seeded migration verification passed (2026-09-20): Flyway V1–V3 succeeded, seed created 10,000 employees and 10,000 salary-history rows, and required indexes were present.
- [x] Frontend production build verified locally; deployment remains unverified.
- [x] Final self-review completed.
- [ ] Live deployment and demo video supplied by the applicant.

## Completed checkpoint — foundation and backend

- [x] Requirements and scope captured in `docs/requirements.md`.
- [x] Java 21-compatible Spring Boot 3.5 Maven project created.
- [x] Flyway relational schema for departments, employees, and salary history.
- [x] JPA entities, repositories, DTOs, validation, and service layer.
- [x] Employee listing with server-side pagination, search, filters, and allow-listed sorting.
- [x] Employee create, edit, detail, status change, and salary-history APIs.
- [x] Dashboard summary, department, country, and salary-distribution APIs.
- [x] Deterministic seed profile design for 10,000 employees.
- [x] Environment-driven CORS, database settings, profiles, and health endpoint configuration.
- [x] Centralized validation, not-found, duplicate, and unexpected-error responses.
- [x] Backend unit tests: service rules, clock-controlled salary history, pagination metadata, dashboard mapping, and validation/error handling; API integration coverage is implemented but Docker-dependent.
- [x] Backend checkpoint committed as `139e975` and `12cac67`.
- [ ] Backend Testcontainers suite: Testcontainers 1.21.4 started successfully; 9 tests passed and `EmployeeApiIntegrationTest.rejectsStaleUpdateWithConflict` failed because status was 200 instead of 409.

## Current frontend stage

- [x] Angular 20 standalone workspace scaffolded.
- [x] PrimeNG 20 component library and Aura theme installed.
- [x] Responsive application shell and route navigation created.
- [x] Runtime API URL configuration created in `frontend/public/app-config.js`.
- [x] Data models and typed HTTP API service created.
- [x] Dashboard created with live KPI, department, country, currency, and salary-distribution data.
- [x] Employee directory created with live search, filters, sorting, pagination, loading, error, and empty states.
- [x] Employee detail page and salary-history table.
- [x] Add/edit employee form with validation and salary-change reason.
- [x] Deactivation confirmation and success/error feedback verification.
- [x] Frontend unit tests for API service, filtering, dashboard failure handling, employee detail status behavior, form create/update/conflict handling, department-load failure, and status behavior.
- [x] Frontend production build and ChromeHeadless test run: 15 tests passing (verified 2026-09-20).

## Remaining implementation and review

- [x] Add backend Dockerfile, root `docker-compose.yml`, and environment examples.
- [x] Add concise architecture, database, testing, seeding, performance, deployment, trade-off, and AI workflow documents.
- [x] Add root README with local setup, API overview, deployment placeholders, and submission checklist.
- [x] Add final review documenting implemented scope, exclusions, limitations, technical debt, and interview discussion points.
- [x] Review for dead code, hard-coded secrets, broken imports, unimplemented UI actions, and mismatched documentation.
- [ ] Run backend `mvn clean test` and `mvn clean verify` in a Docker-enabled environment.
- [x] Run frontend `npm ci`, `npm test`, and production build.
- [x] Docker build and clean PostgreSQL startup verified after the remediation migrations (2026-09-20).
- [x] Create final incremental Git commits after each completed stage.
- [ ] Applicant action: deploy the app, record the demo, and submit repository/live/demo URLs by email.

## Working decisions

- PrimeNG is used instead of Angular Material after the frontend workspace was scaffolded; it provides the table, cards, inputs, tags, paginator, messages, and confirmation UI needed by this product.
- Authentication is intentionally out of scope because the assessment does not define identity or role requirements. A protected deployment should add it before handling real employee data.
- Compensation is grouped by stored currency. No exchange-rate conversion is performed, so cross-currency totals are never presented as one misleading number.
