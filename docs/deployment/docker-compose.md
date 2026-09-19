# Docker Compose deployment guide

## Services

The local stack contains three services:

| Service | Purpose | Host exposure |
|---|---|---|
| `frontend` | Builds the React application and serves it with unprivileged Nginx | `${FRONTEND_PORT:-8080}` |
| `backend` | Runs the Spring Boot API and Flyway migrations | `${BACKEND_PORT:-8082}` |
| `database` | PostgreSQL 17 with a named data volume | Not published |

Nginx proxies `/api`, `/public`, `/student`, and `/roles` to the backend. The browser therefore uses one frontend origin and does not need to know Docker service names.

The frontend and backend share `application-network`. The backend and PostgreSQL share the internal-only `database-network`. PostgreSQL has no host port mapping.

## Quick start

Docker Desktop or another Docker Engine with Compose is required. From the repository root:

```powershell
Copy-Item .env.example .env
docker compose up --build -d --wait
```

Open `http://localhost:8080`. The default administrator login is:

- Email: `admin@iut-dhaka.edu`
- Password: `admin12345`

With the default `demo` profile, the application creates a connected presentation dataset containing five personas, five classrooms, eleven routine entries, five bookings in multiple states, and a pending role request. Existing records are preserved, and restarting the backend does not duplicate the demo records. See the [presentation data guide](../demo/presentation-data.md) for all credentials and a suggested walkthrough.

Do not commit `.env`. For anything beyond this academic deployment, replace the default PostgreSQL password, JWT secret, and demo password.

## Port conflicts

If port 8080 or 8082 is already occupied, edit `.env`. When changing the frontend port, update its CORS origin as well:

```dotenv
FRONTEND_PORT=18080
BACKEND_PORT=18082
CORS_ALLOWED_ORIGINS=http://localhost:18080
```

The frontend remains on container port 8080 and the backend remains on container port 8082; only host mappings change.

## Status, health, and logs

```powershell
docker compose ps
docker compose logs -f backend frontend database
```

Compose waits for PostgreSQL before starting the backend and waits for the backend before starting the frontend.

- PostgreSQL uses `pg_isready`.
- The backend uses `/actuator/health`, including database connectivity.
- The frontend uses `/healthz`.

Only the Actuator health endpoint is exposed. Health details are not returned publicly.

## Rebuild after code changes

```powershell
docker compose build --pull
docker compose up -d --wait
```

Both application Dockerfiles use multi-stage builds and BuildKit dependency caches. Maven, Node.js, source files, and development dependencies do not remain in the runtime images. Both application containers run as non-root users.

The explicit `--pull` refreshes base-image layers before rebuilding. Runtime containers also use `no-new-privileges`, and Docker JSON logs rotate after 10 MB with three retained files.

## Release gate

Before an academic/demo release, run the disposable clean-stack verification from the repository root:

```powershell
./scripts/release-check.ps1
```

It builds refreshed images, creates a temporary PostgreSQL database, runs the full booking and role workflow, and removes its isolated resources afterward. See the [release-readiness guide](../release/release-readiness.md) for its assertions and the remaining internet-facing production requirements.

## Stop and preserve data

```powershell
docker compose down
```

This removes containers and networks but preserves `unispace_postgres-data`. The next `docker compose up` reuses the database, Flyway history, accounts, and bookings.

To remove the database permanently:

```powershell
docker compose down --volumes
```

This is destructive and cannot be undone without a backup.

## Backup and restore

Create a SQL backup:

```powershell
docker compose exec -T database pg_dump -U unispace -d unispace -Fc > unispace.dump
```

Restore into an empty running database:

```powershell
Get-Content -AsByteStream unispace.dump | docker compose exec -T database pg_restore -U unispace -d unispace --clean --if-exists
```

Use the configured `POSTGRES_USER` and `POSTGRES_DB` instead when their defaults were changed.

## Configuration reference

| Variable | Default | Purpose |
|---|---|---|
| `POSTGRES_DB` | `unispace` | PostgreSQL database |
| `POSTGRES_USER` | `unispace` | PostgreSQL user |
| `POSTGRES_PASSWORD` | `unispace` | PostgreSQL password |
| `JWT_SECRET` | Academic demo value | Token-signing secret |
| `JWT_EXPIRATION_MS` | `1800000` | Token lifetime |
| `FRONTEND_PORT` | `8080` | Frontend host port |
| `BACKEND_PORT` | `8082` | Backend host port |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:8080` | Origins allowed to call the published backend directly |
| `SPRING_PROFILES_ACTIVE` | `demo` | Enables connected presentation data creation |
| `DEMO_ADMIN_EMAIL` | `admin@iut-dhaka.edu` | Demo administrator email |
| `DEMO_ADMIN_PASSWORD` | `admin12345` | Demo administrator password |
| `DEMO_ADMIN_NAME` | `Demo Administrator` | Demo administrator name |
