# AI Workflow

## Tools and use

This repository was implemented with an AI coding assistant operating in the workspace. AI was used to turn the explicit assessment checklist into a staged implementation plan, propose a minimal relational model, draft Spring Boot/Angular boilerplate, write focused tests, and review compiler/test failures.

Representative prompts included:

- “Design a modular-monolith schema for employees, departments, and append-only salary history for 10,000 rows.”
- “Implement Spring service rules for duplicate employee values, pagination, status changes, and salary-history snapshots.”
- “Build a typed Angular HR dashboard with server-side filters and a PrimeNG component library.”
- “Run the tests, explain the failure, and fix the root cause without hiding the failure.”

## Review and verification

The assistant reviewed the generated code against the written requirements, kept DTO/entity boundaries, constrained sort fields, added validation and safe exception responses, and verified behavior with Maven tests, a Flyway/H2 API integration test, Angular ChromeHeadless tests, and a production Angular build. A real schema mismatch (`CHAR(3)` versus Hibernate's `VARCHAR(3)`) was found by the integration test and corrected before proceeding.

Generated UI boilerplate was replaced with domain-specific pages, labels, empty/error states, multi-currency explanations, salary-history behavior, and runtime API configuration. The generated Angular starter title test was removed because it asserted placeholder content.

## Rejected or intentionally limited output

No authentication, payroll, currency conversion, microservices, Redis, Kafka, Elasticsearch, or giant seed SQL file was added because those are outside the stated assessment scope. The dashboard does not fabricate a single cross-currency total; it keeps values grouped by stored currency.

The applicant should perform a final human review, run the deployment steps, configure authentication before using real salary data, and record the demo. This document describes the actual assistant workflow; it does not claim a human approved every generated line.
