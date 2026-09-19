# Deployment

The intended topology is a static Angular host (Vercel, Netlify, or Cloudflare Pages), a Spring Boot container/service (Render, Railway, or equivalent), and a managed PostgreSQL provider (Neon, Supabase, or equivalent). Provider names are examples; the application uses standard environment variables.

## 1. Create PostgreSQL

Create a PostgreSQL 15+ database and record its JDBC URL, username, and password. Ensure the provider accepts external connections from the backend service.

## 2. Configure the backend

Set these backend variables:

```text
DATABASE_URL=jdbc:postgresql://HOST:5432/DATABASE?sslmode=require
DATABASE_USERNAME=...
DATABASE_PASSWORD=...
CORS_ALLOWED_ORIGINS=https://YOUR-FRONTEND-DOMAIN
PORT=8080
SPRING_PROFILES_ACTIVE=prod
```

Never put credentials in Git. On first startup Flyway applies migrations automatically. Do not enable the `seed` profile for a database that already contains data.

## 3. Deploy the backend

Build from the repository's `backend/` directory with the included `Dockerfile`, or use a Java 21 build command `mvn -B package` and start `java -jar target/salary-management-api-1.0.0.jar`. Configure the service health check as `/actuator/health`.

Verify `https://YOUR-API/actuator/health` returns `{"status":"UP"}` and that `GET /api/departments` responds.

## 4. Configure and deploy the frontend

Before the static build, replace `frontend/public/app-config.js` with the deployed API URL:

```js
window.APP_CONFIG = { API_BASE_URL: 'https://YOUR-API/api' };
```

Alternatively, have the host's build step generate that file from `API_BASE_URL`. Set `API_BASE_URL` in the frontend host, run `npm ci`, then `npm run build`. Publish `frontend/dist/frontend/browser` (or the output folder shown by the Angular CLI). Configure SPA fallback to `index.html` so deep links work.

## 5. Verify production

Open the live UI, load Dashboard, search an employee, open details, edit a salary, confirm salary history, and test deactivation/reactivation. Check browser network requests for the deployed API host and confirm the backend `CORS_ALLOWED_ORIGINS` exactly matches the frontend origin. If demo data is required, use the one-time `seed` startup against a fresh database and then remove the profile.
