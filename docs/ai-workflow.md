# AI Workflow

## Tools and use

This repository was implemented with an AI coding assistant operating in the workspace. AI was used to turn the explicit assessment checklist into a staged implementation plan, propose a minimal relational model, draft Spring Boot/Angular boilerplate, write focused tests, and review compiler/test failures.

Representative prompts included:

- “Design a modular-monolith schema for employees, departments, and append-only salary history for 10,000 rows.”
- “Implement Spring service rules for duplicate employee values, pagination, status changes, and salary-history snapshots.”
- “Build a typed Angular HR dashboard with server-side filters and a PrimeNG component library.”
- “Run the tests, explain the failure, and fix the root cause without hiding the failure.”

## Concrete AI Review Examples

### Example 1

Problem: invalid UUID and enum query values reached the generic exception handler and returned HTTP 500.

AI-assisted suggestion: add explicit Spring MVC conversion, malformed-body, validation, and integrity-violation handlers with one `ApiError` shape.

Engineering concern: a broad exception handler must not hide real server failures or leak database messages.

What was changed: `GlobalExceptionHandler` now maps request conversion and JSON errors to 400, missing records to 404, friendly and database-backed uniqueness conflicts to 409, and leaves unhandled exceptions as safe 500 responses.

How it was tested: MockMvc integration coverage exercises invalid UUID, invalid enum, malformed JSON, bean validation, missing employee, and duplicate employee number/email responses.

### Example 2

Problem: fixed 50k/100k/150k dashboard bands made salary distribution misleading for the seeded INR data.

AI-assisted suggestion: do not add exchange-rate infrastructure; instead, retain stored-currency analytics and make bands currency-specific.

Engineering concern: the bands must be explicit about their currency and not imply a conversion source.

What was changed: the aggregate query uses documented thresholds for the six demo currencies, and the dashboard calls the view “Salary distribution by currency.”

How it was tested: the API integration test creates USD and INR salaries and verifies they land in their respective band labels.

### Example 3

Problem: the frontend shipped an explicit localhost API URL, which would break a static production deployment.

AI-assisted suggestion: use a same-origin runtime default with a development proxy and replace the small runtime config file at deployment.

Engineering concern: production must not silently use localhost, while local development still needs a straightforward backend target.

What was changed: the committed runtime config uses `/api`, `npm start` proxies it to `http://localhost:8080`, and deployment instructions require replacing the emitted config with the API’s HTTPS URL.

How it was tested: configuration and request-construction component tests run as part of the frontend suite; deployment-time URL replacement still requires host-specific verification.

## Review and verification

The assistant reviewed the generated code against the written requirements, kept DTO/entity boundaries, constrained sort fields, and used Maven tests plus a Flyway/H2 API integration test to verify backend behavior. A real schema mismatch (`CHAR(3)` versus Hibernate's `VARCHAR(3)`) was found by the integration test and corrected before proceeding. Frontend build/test claims are recorded only after their commands complete successfully in the local environment.

Generated UI boilerplate was replaced with domain-specific pages, labels, empty/error states, multi-currency explanations, salary-history behavior, and runtime API configuration. The generated Angular starter title test was removed because it asserted placeholder content.

## Rejected or intentionally limited output

No authentication, payroll, currency conversion, microservices, Redis, Kafka, Elasticsearch, or giant seed SQL file was added because those are outside the stated assessment scope. The dashboard does not fabricate a single cross-currency total; it keeps values grouped by stored currency.

The applicant should perform a final human review, run the deployment steps, configure authentication before using real salary data, and record the demo. This document describes the actual assistant workflow; it does not claim a human approved every generated line.
