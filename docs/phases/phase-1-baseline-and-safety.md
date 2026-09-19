# Phase 1 — Baseline and safety fixes

Status: Complete  
Completed: 2026-08-28

## Objective

Create a secure, reproducible baseline before changing the domain model, database, or deployment architecture.

## Implemented changes

### Runtime and configuration

- Standardized the backend on Java 21 LTS.
- Moved the server port, MongoDB URI, allowed CORS origins, JWT secret, and JWT expiration to configuration backed by environment variables.
- Removed the committed JWT signing key. The backend now refuses to start unless `JWT_SECRET` is supplied and contains at least 32 bytes.
- Added backend and frontend `.env.example` files while excluding real `.env` files from source control.
- Added a single frontend API URL helper. Production can use a configured API origin or same-origin requests.
- Added a Vite development proxy so local frontend code no longer needs embedded backend URLs.

### Authentication and authorization

- Changed Spring Security to stateless session handling.
- Restricted user-administration and role-request review operations to administrators.
- Restored the bearer token on the frontend's create-user request.
- Made malformed bearer tokens fail authentication without breaking the filter chain.
- Replaced authentication responses containing the persistence `User` entity with a safe response containing only ID, email, and roles.
- Marked the persisted password as write-only for JSON as defense in depth.
- Invalid login attempts now return a structured `401 Unauthorized` response.

### Booking ownership

- Cancellation now queries by both booking ID and authenticated user ID.
- A user cannot cancel another user's booking, even if the other booking ID is known.
- Successful cancellation now returns `204 No Content`.

### Error handling

- Removed the booking controller path that returned `null` after an exception.
- Added a global JSON API error format and safe handling for bad requests and unexpected errors.
- Unexpected server errors are logged while their internal details are not exposed to clients.

### Frontend session restoration

- Protected pages now wait for the initial authentication check before redirecting.
- This prevents valid users and administrators from being redirected away during a page refresh.

## Runtime configuration

### Backend

| Variable | Required | Default | Purpose |
|---|---:|---|---|
| `JWT_SECRET` | Yes | None | JWT HMAC signing secret; minimum 32 bytes |
| `JWT_EXPIRATION_MS` | No | `1800000` | Token lifetime in milliseconds |
| `SERVER_PORT` | No | `8082` | Backend HTTP port |
| `MONGODB_URI` | No | `mongodb://localhost:27017/UniSpace` | Phase 1 database connection |
| `CORS_ALLOWED_ORIGINS` | No | Local Vite origins | Comma-separated browser origins |

The backend example is in `Backend/UniSpace/.env.example`. Spring Boot reads environment variables supplied by the shell, IDE, service manager, or a future container definition; it does not automatically import the example file.

### Frontend

| Variable | Required | Default | Purpose |
|---|---:|---|---|
| `VITE_API_BASE_URL` | No | Empty/same origin | Public backend origin used in built frontend code |
| `VITE_DEV_PROXY_TARGET` | No | `http://localhost:8082` | Backend target for local Vite development |

The frontend example is in `Frontend/classflo-booking/.env.example`.

## API behavior changes

- Login and `/api/auth/me` now return a safe `user` object: `id`, `email`, and `roles`.
- Password hashes are never serialized.
- `POST /api/users` requires an authenticated administrator.
- `DELETE /api/me/bookings/{id}` returns `204` for an owned booking and `404` when no booking belonging to that user exists.
- Handled API failures use this shape:

```json
{
  "timestamp": "2026-08-28T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid request",
  "path": "/example"
}
```

## Verification

- Backend compiles on Java 21.
- Maven test suite: 5 passed, 0 failed.
- Tests cover application startup, unauthenticated admin user creation, password serialization, owned cancellation, and cross-user cancellation denial.
- Frontend TypeScript check passes.
- Frontend production build passes.
- Frontend ESLint still reports the pre-existing baseline of 8 errors and 11 warnings, primarily generated UI component typing, existing `any` types, and hook dependency warnings. Phase 1 introduced no new lint category or error in its API helper.

## Deferred to later phases

- Transactional user/profile creation and role/profile synchronization.
- Booking overlap, duration, date, classroom, and concurrency validation.
- Domain DTO conversion beyond authentication responses.
- Native date/time model types and enums.
- PostgreSQL, database migrations, and data conversion.
- Dockerfiles, Compose, production reverse proxy, and deployment health checks.

## Operational note

The JWT key previously committed in source must be considered compromised. Deployments should use a newly generated random secret, which will invalidate tokens signed with the old key.
