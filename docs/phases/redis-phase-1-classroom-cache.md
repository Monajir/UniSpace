# Redis Phase 1 — Classroom Catalogue Cache

**Status:** Complete  
**Completed:** 2026-09-17

## Scope

Added Redis caching to the available-classroom catalogue. PostgreSQL remains the source of truth. No frontend changes or database migrations are required. Login and booking request limiting are not included in this phase.

## Behaviour

- `ClassroomService.getAllAvailableClassrooms()` first reads the Redis snapshot.
- On a cache miss, it queries PostgreSQL and stores typed classroom JSON with a configurable expiration, defaulting to 60 seconds. Equipment and availability are included.
- Cache key: `unispace:classroom-catalogue:v1:available`.
- Classroom persistence callbacks invalidate after commit; repository advice also covers collection-only equipment edits and repository bulk deletes. Rollbacks do not clear the cache.
- Redis read, write, eviction, and JSON-decoding errors do not fail catalogue requests. Reads fall back to PostgreSQL. Connection and command timeouts are 500 ms each; fallback can add latency during an outage.
- Booking validation and pessimistic database locking continue to read PostgreSQL directly. Cached catalogue data is never authoritative for booking conflicts.
- Direct SQL changes bypass application invalidation. Concurrent cache fills or failed invalidation can briefly leave stale catalogue data; expiration bounds each cached snapshot's lifetime. Strictly current availability is checked against PostgreSQL before booking.
- There is currently no classroom-administration mutation endpoint; the hooks cover existing repository-based initialization and future repository-based edits without adding one.

## Docker and Configuration

Compose adds `redis:7.4-alpine` on the internal database network, without publishing a host port. Redis uses a 64 MB data-memory limit and LRU eviction. Persistence is disabled because the cache is disposable; PostgreSQL data and volumes are unchanged. The memory limit is not a total container RAM limit.

Redis is intentionally not a backend startup dependency or readiness requirement, so a cache outage does not make an otherwise working app unhealthy. Compose still checks the Redis container's own health.

| Variable | Compose default | Standalone backend default |
|---|---|---|
| `CLASSROOM_CACHE_ENABLED` | `true` | `false` |
| `CLASSROOM_CACHE_TTL` | `60s` | `60s` |
| `REDIS_HOST` | `redis` | `localhost` |
| `REDIS_PORT` | `6379` | `6379` |

TTL must be positive. The example environment files document these options. Disable caching with `CLASSROOM_CACHE_ENABLED=false` and recreate the backend if needed.

## Start the Updated App

```powershell
docker compose up -d --build --wait
```

This rebuilds the backend with its new dependencies. Starting only Redis does not update an already running backend image.

## Presentation Demonstration

1. Open or refresh the classroom catalogue to populate the cache.
2. Inspect the cached snapshot and remaining lifetime:

```powershell
docker compose exec -T redis redis-cli GET unispace:classroom-catalogue:v1:available
docker compose exec -T redis redis-cli TTL unispace:classroom-catalogue:v1:available
```

3. Refresh before expiration; the catalogue uses the existing snapshot rather than querying PostgreSQL.
4. After expiration, another catalogue request repopulates the snapshot. A Redis restart discards the cache without discarding users or bookings.
5. Optionally stop Redis, refresh the catalogue to demonstrate database fallback, and restore Redis afterward:

```powershell
docker compose stop redis
# Refresh the catalogue in the browser.
docker compose start redis
```

TTL checks alone show cache presence, not database-query counts. Cache-hit behaviour is verified by the regression tests.

## Verification

- Full backend suite: **68 tests, 0 failures, 0 errors**.
- Tests cover cache hits skipping database queries, misses and TTL writes, classroom/equipment JSON round trips, disabled caching, corrupt JSON, Redis read/write/eviction failures, post-commit invalidation, and rollback behaviour.
- Database integration test verifies actual classroom creation, scalar edits, equipment-only edits, and deletion trigger invalidation.
- Docker Compose configuration validation passed.
- Started only the new Redis service; its health check passed and `redis-cli ping` returned `PONG`.
- Existing frontend/backend/database containers were not rebuilt or restarted during verification. End-to-end caching in the rebuilt Docker backend remains an operational check after the command above.

