# Phase 6 — Dockerize the application

Status: Complete  
Completed: 2026-08-29

## Objective

Package the React frontend, Spring Boot backend, and PostgreSQL database as a reproducible local container stack with safe networking, persistent storage, startup readiness, and documented operations.

## Implemented changes

### Backend image

- Added a multi-stage Dockerfile using Maven with Java 21 for compilation and a Java 21 JRE-only Alpine runtime.
- Added a BuildKit Maven cache so dependencies persist between image builds without entering the final image.
- The runtime image contains only the packaged application and JRE.
- The backend runs as the dedicated non-root `unispace` user.
- JVM container memory sizing uses `MaxRAMPercentage=75.0`.
- Added a focused backend `.dockerignore` to exclude build output, local secrets, editor files, and repository metadata.

### Frontend image and reverse proxy

- Added a Node 22 build stage using reproducible `npm ci` installation.
- Added an unprivileged Nginx 1.27 Alpine runtime containing only the generated static application.
- The frontend container runs as numeric non-root user 101.
- Nginx supports React SPA deep links through an `index.html` fallback.
- Long-lived immutable caching is enabled for hashed assets.
- `/api`, `/public`, `/student`, and `/roles` are proxied to the backend through the Compose network.
- The built frontend keeps `VITE_API_BASE_URL` empty, giving browsers a same-origin API contract.
- Added basic response security headers and a dedicated `/healthz` endpoint.

### Compose orchestration

- Added `compose.yaml` for PostgreSQL 17, the backend, and the frontend.
- Added dependency-aware startup: PostgreSQL health gates the backend, and backend health gates the frontend.
- Added restart policies and init processes for application services.
- PostgreSQL state is stored in the named `postgres-data` volume.
- PostgreSQL is not published to the host and is attached only to an internal database network.
- The backend bridges the private database network and frontend application network.
- Host frontend/backend ports and application credentials are configurable from a root `.env` file.
- Added a checked-in root `.env.example` and root ignore rules that exclude real environment files and generated build output.

### Health and readiness

- Added Spring Boot Actuator with only the health endpoint exposed.
- The health endpoint is publicly readable for container orchestration but does not expose component details.
- Backend health includes database connectivity, ensuring an application is not marked ready when PostgreSQL is unavailable.
- PostgreSQL uses `pg_isready`; Nginx uses its local `/healthz` endpoint.
- Existing `/public/health-check` remains available for application-level proxy verification.

### Migration compatibility

- The Phase 5 migration profile runs successfully as a one-off Compose backend job.
- Export data can be mounted read-only while its report is written to a separate mounted output directory.
- The normal backend is not required for migration; only PostgreSQL must be started first.

## Verification

- Docker Compose v5.4 validated the complete interpolated configuration.
- Backend and frontend images built successfully from clean multi-stage builds.
- Runtime image inspection confirmed backend user `unispace` and frontend user `101`.
- Runtime image sizes were approximately 128 MB for the backend and 21 MB for the frontend.
- PostgreSQL 17.10, backend, and frontend all reached Compose `healthy` status.
- Flyway created schema version 1 on first startup and recognized it as current after container recreation.
- Actuator reported `UP` with live PostgreSQL connectivity.
- Nginx served the application, resolved `/profile` as an SPA deep link, and proxied backend requests.
- Demo administrator login through the frontend proxy returned a JWT and linked user/profile data.
- PostgreSQL inspection confirmed the administrator role and profile foreign key.
- The database had no published host ports, and its Docker network reported `internal=true`.
- Removing all containers and networks without deleting volumes preserved exactly one administrator across stack recreation.
- A containerized Phase 5 dry run converted six fixture documents and wrote a valid report through mounted directories.
- Backend Maven suite: 27 passed, 0 failed.
- Frontend production build passes; its existing bundle-size and stale Browserslist-data advisories remain non-blocking.

The test host already used port 8080, so end-to-end frontend verification used the documented override `FRONTEND_PORT=18080` with the matching CORS origin. The Compose default remains 8080.

## Operations

Build, startup, logs, persistence, backup/restore, migration jobs, configuration, and destructive volume cleanup are documented in the [Docker Compose deployment guide](../deployment/docker-compose.md).

## Deferred to Phase 7

- A final automated release-readiness run from a clean checkout and empty Docker state.
- Broader end-to-end workflow checks for signup, booking request, approval, rejection, and cancellation.
- Dependency and container vulnerability review.
- Production-specific TLS, external secret management, resource limits, and observability decisions.
