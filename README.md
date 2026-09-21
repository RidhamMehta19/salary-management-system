# Salary Management System

SalaryFlow is a focused employee salary-management application for an HR Manager. It replaces spreadsheet workflows with searchable employee records, salary history, and database-backed compensation insights for an organisation of approximately 10,000 people.

## Live Demo

- Application: https://salary-management-portal-enhance.netlify.app
- API health: https://salary-management-system-iwe0.onrender.com/actuator/health
- Demo video: https://yorecord.com/view?uid=7a883efd-c7e4-430d-bc57-0094abf26e97

Free-tier hosting may take a while to wake up. If the first load is slow, wait about 60 seconds for the backend to wake up and refresh.

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

- Netlify hosts the Angular frontend. Use `frontend` as the base directory and `dist/frontend` as the publish directory.
- Render hosts the Dockerized Spring Boot backend from the `backend` root directory. Configure the health check path as `/actuator/health`.
- Neon provides the managed PostgreSQL database.

Backend environment variable names:

```text
DATABASE_URL
DATABASE_USERNAME
DATABASE_PASSWORD
CORS_ALLOWED_ORIGINS
SPRING_PROFILES_ACTIVE   # seed on first boot only
JAVA_TOOL_OPTIONS
```

The frontend API URL is set through `app-config.js` at build time. See [deployment](docs/deployment.md) for provider setup details.

## Project structure

```text
backend/       Spring Boot API, Flyway migrations, seed profile, tests
frontend/      Angular + PrimeNG application and tests
docs/          Requirements, architecture, operations, testing, AI, and review artifacts
docker-compose.yml
```

## Design decisions and trade-offs

- The application is a modular monolith: it keeps deployment simple while separating employee, salary, dashboard, and department responsibilities.
- Salary-history snapshots preserve the compensation state and reason for each change; salary figures remain grouped by currency to avoid false precision.
- Optimistic locking uses the `version` field, and stale edits return HTTP 409.
- Case-insensitive unique indexes protect employee numbers and email addresses after canonicalization.
- Deterministic pagination keeps directory results stable while clients move through pages.
- `pg_trgm` indexes provide the search path for the 1M-row employee dataset.
- Testcontainers PostgreSQL integration tests exercise the API against the same Flyway-managed database technology used in deployment.
- The background batched idempotent seeder is designed for slow hosts and high-latency databases.
- Authentication, authorization, and audit identity are intentionally out of scope, but are required before real salary data is used.

The AI workflow and review boundaries are documented in [ai-workflow](docs/ai-workflow.md). See [testing strategy](docs/testing-strategy.md) for coverage intent and [final review](docs/final-review.md) for known limitations and interview discussion points.
