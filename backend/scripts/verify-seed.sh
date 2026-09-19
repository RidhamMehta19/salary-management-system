#!/usr/bin/env bash
set -euo pipefail

: "${DATABASE_URL:?Set DATABASE_URL to a PostgreSQL connection string before running this check.}"

actual="$(psql "$DATABASE_URL" --tuples-only --no-align --field-separator='|' --command '
  SELECT
    (SELECT count(*) FROM employees),
    (SELECT count(DISTINCT employee_number) FROM employees),
    (SELECT count(DISTINCT email) FROM employees),
    (SELECT count(*) FROM salary_history);
')"

if [[ "$actual" != "10000|10000|10000|10000" ]]; then
  echo "Seed verification failed: expected 10000|10000|10000|10000, got $actual" >&2
  exit 1
fi

echo "Seed verification passed: employees=10000, unique employee numbers=10000, unique emails=10000, salary history=10000"
