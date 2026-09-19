# Seeding 10,000 Employees

The application includes a deterministic `seed` Spring profile. It creates eight departments and 10,000 realistic-looking employee records in batches of 250. The data uses fixed names, offices, currencies, salary ranges, employment statuses, and a fixed random seed (`20260919L`). Employee IDs and emails are unique. An initial salary-history snapshot is created for every employee.

The seed is intentionally skipped when any employee already exists, so restarting the application cannot duplicate data.

## Local Docker flow

Start PostgreSQL and the API. Local Compose enables the `seed` profile automatically; PostgreSQL is kept internal to the Compose network, so no host database port is reserved:

```bash
docker compose up --build -d
```

After startup, verify the records with `curl http://localhost:8080/api/employees?size=1`. The first startup creates 10,000 employees; later restarts skip seeding when records already exist.

If host tools need database access, add a free host mapping such as `"5434:5432"` to the PostgreSQL service. The backend container always connects to `postgres:5432` on the internal Compose network.

For a fresh database, start the backend with the seed profile:

```bash
SPRING_PROFILES_ACTIVE=seed mvn -f backend/pom.xml spring-boot:run
```

If the API is running in Docker, set `SPRING_PROFILES_ACTIVE=seed` on the `backend` service and recreate that service once. Verify the result with `GET /api/employees?size=1` and `GET /api/dashboard/summary`.

Seeding is demo data only. Production environments should use a reviewed import process and should not enable this profile against an existing database.
