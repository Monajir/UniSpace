# Phase 2 — Stabilize the domain model

Status: Complete  
Completed: 2026-08-28

## Objective

Separate the HTTP API contract from MongoDB persistence details so that the application can move to PostgreSQL without forcing a simultaneous frontend rewrite.

## Implemented changes

### Database-independent API boundary

- Added dedicated request and response DTOs for bookings, schedules, classrooms, profiles, users, pending bookings, and pending role requests.
- Added a central API mapper between the existing MongoDB entities and the new DTOs.
- Controllers no longer expose `ObjectId` values or Mongo persistence entities in the main booking, classroom, authentication, profile, user-management, or pending-request responses.
- API identifiers are ordinary strings named `id`. MongoDB `ObjectId` remains an internal persistence concern during the transition.
- Booking creation no longer accepts a client-provided `user_id`; the authenticated user remains the source of ownership.

### Typed domain values

- Added `BookingStatus` for `pending`, `booked`, `rejected`, and the UI-only `available` state.
- Added `Role` for `STUDENT`, `CR`, `FACULTY`, and `ADMIN`.
- Replaced status and role magic strings in the affected services and security rules with enums.
- Booking and routine API DTOs use `LocalDate` and `LocalTime`, serialized in stable `yyyy-MM-dd` and `HH:mm` formats.
- Legacy MongoDB date/time strings are converted only at the mapper boundary, keeping the current database operational until PostgreSQL is introduced.
- Classroom capacity is exposed as an integer instead of leaking the existing string storage representation.

### Frontend contract corrections

- Updated booking creation to match the backend's direct booking response rather than expecting a nonexistent `{ data: ... }` wrapper.
- Removed `user_id` from the frontend booking request.
- Updated administrator user and role-request screens to consume stable `id` fields instead of Mongo-style `_id` fields.

### Removed obsolete response models

- Removed the old authentication, user-management, and pending-role response classes superseded by the new DTO layer.
- Retained the student dashboard's purpose-specific presentation responses because they do not expose database-specific identifiers or persistence entities.

## API behavior changes

- Booking, classroom, user, profile, and pending-request IDs are serialized as strings.
- Managed users and pending roles now expose `id`, not `_id`.
- Booking status values remain lowercase for frontend compatibility.
- Role values remain uppercase for frontend and Spring Security compatibility.
- `POST /api/bookings/room/book` returns the created booking directly with HTTP `201`.
- Dates and times have explicit, database-independent JSON formats.

## PostgreSQL design decisions

### Identifier strategy

PostgreSQL entities will use UUID primary keys. Until Phase 4, existing MongoDB hexadecimal IDs continue to pass through the API as strings. Because clients treat IDs as opaque strings, changing the backing value from a MongoDB hex ID to a UUID will not require another API shape change.

### Administrator bootstrap

Existing MongoDB administrator records do not need to be preserved for this academic deployment. The PostgreSQL development/demo environment may create a demo administrator when no administrator exists. The seed must be repeatable and limited to an explicit development/demo profile; it must not become an unauthenticated public bootstrap endpoint.

### JWT handling

No emergency key rotation is required for this academic project. The environment-based JWT configuration from Phase 1 remains in place because it is reproducible for Docker and avoids coupling runtime configuration to source code.

## Verification

- Backend Maven suite: 8 passed, 0 failed.
- Added mapper contract tests for typed booking requests, legacy persistence conversion, stable string IDs, canonical date/time JSON, lowercase booking statuses, and role persistence values.
- Spring application context and Phase 1 security/ownership tests continue to pass.
- Frontend TypeScript check passes.
- Frontend production build passes.
- The production build still reports the existing large-bundle advisory; this is a performance warning and does not block the database or Docker work.

## Deferred to later phases

- Transactional account/profile and role/profile consistency (Phase 3).
- Booking overlap, concurrency, duration, date, and classroom integrity rules (Phase 3).
- Replacing MongoDB entities and repositories with JPA/PostgreSQL equivalents (Phase 4).
- PostgreSQL schema migrations and the demo administrator seed (Phase 4).
- Optional migration of existing academic/demo data (Phase 5).
- Containers, Compose orchestration, and deployment health checks (Phase 6).
