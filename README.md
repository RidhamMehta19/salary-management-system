# Salary Management System

SalaryFlow is a focused employee salary-management application for an HR Manager. It replaces spreadsheet workflows with searchable employee records, salary history, and database-backed compensation insights for an organisation of approximately 10,000 people.

Live application: `LIVE_APP_URL`  
Demo video: `VIDEO_DEMO_URL`

## Features

- Dashboard with active headcount, total compensation and average salary grouped by stored currency, department/country breakdowns, and salary bands.
- Employee directory with server-side pagination, name/ID search, department/country/status filters, and allow-listed sorting.
- Employee create/edit/detail workflows, deactivation/reactivation, validation, and user feedback.
- Append-only salary-history snapshots with effective dates and change reasons.
- Fully deterministic 10,000-employee demo seed, PostgreSQL migrations, health endpoint, and environment-driven CORS.

## Architecture and stack

This is a modular monolith: Angular + PrimeNG → Spring REST controllers → services → Spring Data JPA repositories → PostgreSQL. Flyway manages the schema and aggregate queries keep dashboard work in the database.

- Backend: Java 21, Spring Boot 3.5, Spring Web, Spring Data JPA, Bean Validation, Flyway, PostgreSQL, JUnit 5, Mockito.
- Frontend: Angular 20 standalone components, TypeScript, PrimeNG 20, RxJS.
- Deployment: static frontend host + Spring Boot container/service + managed PostgreSQL.

See [architecture](docs/architecture.md), [database design](docs/database-design.md), and [trade-offs](docs/tradeoffs.md) for the reasoning behind these choices.

## Local development

Prerequisites: Java 21, Maven 3.9+, Node.js 20+, npm, and Docker for the easiest PostgreSQL setup.

```bash
docker compose up --build -d
npm --prefix frontend ci
npm --prefix frontend start
```

The UI runs at `http://localhost:4200`; the API runs at `http://localhost:8080`. Docker PostgreSQL is intentionally not published to the host because the backend connects over the internal Compose network. Local Compose enables the deterministic `seed` profile and creates 10,000 employees on the first empty database. Flyway creates the schema on API startup. If host tools need direct database access, add a free mapping such as `5434:5432` to the PostgreSQL service.

## Seed 10,000 employees

For Docker Compose, the `seed` profile is enabled automatically. For a backend started outside Docker, use the profile only against an empty demo database:

```bash
SPRING_PROFILES_ACTIVE=seed mvn -f backend/pom.xml spring-boot:run
```

The profile is fully deterministic (including technical UUIDs and timestamps), batched, idempotent when data already exists, and creates salary-history snapshots. Verify a seeded PostgreSQL database with `DATABASE_URL=... backend/scripts/verify-seed.sh`. Full details are in [seeding](docs/seeding.md).

## Tests and builds

```bash
mvn -f backend/pom.xml test
mvn -f backend/pom.xml verify
npm --prefix frontend test -- --watch=false --browsers=ChromeHeadless
npm --prefix frontend run build
```

The backend integration test uses Testcontainers PostgreSQL and the same Flyway migrations as production. Docker must be available; the test fails clearly when it is not.

Updates require the response `version` in the PUT body; stale versions return HTTP 409. Emails and employee numbers are canonicalized before case-insensitive uniqueness checks. Supported currencies are USD, INR, GBP, EUR, CAD, and AUD. Search uses `pg_trgm` GIN indexes for the 1M-row path.

## API overview

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/employees` | Paginated directory with `page`, `size`, `sort`, `search`, `department`, `country`, `status` |
| GET/POST | `/api/employees/{id}` / `/api/employees` | Read or create employee |
| PUT | `/api/employees/{id}` | Update employee and record salary changes |
| PATCH | `/api/employees/{id}/status` | Deactivate/reactivate or change status |
| GET | `/api/employees/{id}/salary-history` | Read newest-first salary snapshots |
| GET | `/api/dashboard/summary` | Headcount and currency summaries |
| GET | `/api/dashboard/by-department` | Department compensation aggregates |
| GET | `/api/dashboard/by-country` | Country compensation aggregates |
| GET | `/api/dashboard/salary-distribution` | Salary-band counts by currency |
| GET | `/actuator/health` | Deployment health check |

## Deployment

Build the backend with [backend/Dockerfile](backend/Dockerfile) and configure `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `CORS_ALLOWED_ORIGINS`, and `PORT`. The committed frontend config uses safe same-origin `/api`; local `npm start` proxies to `http://localhost:8080`, while deployment must replace the emitted `app-config.js` with the deployed API URL. Follow [deployment](docs/deployment.md) for exact steps.

### Deployment prerequisites

Authentication, authorization, and an audit identity are required before real salary data is used. These are intentionally not implemented in this assessment.

## Project structure

```text
backend/       Spring Boot API, Flyway migrations, seed profile, tests
frontend/      Angular + PrimeNG application and tests
docs/          Requirements, architecture, operations, testing, AI, and review artifacts
docker-compose.yml
```

## Engineering decisions and limitations

Authentication, role-based access, payroll processing, tax/benefit logic, currency conversion, bulk imports, and employee deletion are intentionally out of scope because the assessment does not define them. Salary figures remain grouped by currency to avoid false precision. Add authentication and audit identity before production use with real employee data.

The AI workflow and review boundaries are documented in [ai-workflow](docs/ai-workflow.md). See [testing strategy](docs/testing-strategy.md) for coverage intent and [final review](docs/final-review.md) for known limitations and interview discussion points.
