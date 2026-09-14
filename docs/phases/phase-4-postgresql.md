# Phase 4 — Introduce PostgreSQL

Status: Complete  
Completed: 2026-08-29

## Objective

Replace MongoDB with PostgreSQL as the application's only runtime database, establish a versioned relational schema, and move the consistency mechanisms prepared in Phase 3 into real database transactions and constraints.

## Implemented changes

### PostgreSQL persistence stack

- Removed Spring Data MongoDB and replaced it with Spring Data JPA, the PostgreSQL JDBC driver, and Flyway.
- Converted all six repositories to `JpaRepository` implementations.
- Replaced MongoDB `ObjectId` values with UUID primary and foreign keys throughout the persistence model.
- Kept API identifiers as strings, so the frontend contract remains unchanged while the mapper validates and converts UUID values at the boundary.
- Converted booking dates and times to native `LocalDate` and `LocalTime` values and booking state to a typed enum.
- Disabled Open Session in View and configured Hibernate to validate, rather than create or silently modify, the production schema.

### Versioned relational schema

- Added Flyway migration `V1__create_postgresql_schema.sql` for users, roles, profiles, classrooms, equipment, routines, bookings, and pending role requests.
- Added foreign keys with explicit delete behavior, unique constraints, valid-role/status checks, positive-capacity and time-order checks, and the 75-minute booking limit.
- Added indexes for routine and booking availability lookups.
- Added PostgreSQL's `btree_gist` extension and a partial GiST exclusion constraint that prevents overlapping `BOOKED` intervals for the same classroom and date.
- Recurring routine equipment and user roles are represented by dedicated relational collection tables.

### Transactions and concurrency

- User and profile creation now commit or roll back as one transaction; Phase 3's compensating delete is no longer needed.
- Account deletion and its dependent booking, profile, and pending-role cleanup execute in one transaction.
- Role approval updates the user, profile, and pending request atomically; Phase 3's manual role rollback is no longer needed.
- Booking approval obtains a pessimistic lock on the classroom row, revalidates availability and overlaps, and flushes the approved booking before committing.
- The PostgreSQL exclusion constraint remains the final cross-instance safeguard if two application instances race or application checks are bypassed.
- Persistence constraint failures are exposed through the existing structured `409 Conflict` API response.

### Fresh demo database bootstrap

- Added an opt-in `demo` Spring profile.
- When that profile is active and the database has no administrator, startup creates one demo administrator and its linked profile through the normal transactional account service.
- The seed is conditional and is not active in the default production profile.
- Defaults are `admin@iut-dhaka.edu`, `admin12345`, and `Demo Administrator`; all three can be overridden with `DEMO_ADMIN_EMAIL`, `DEMO_ADMIN_PASSWORD`, and `DEMO_ADMIN_NAME`.

### Configuration

The backend now reads:

- `DATABASE_URL`, defaulting to `jdbc:postgresql://localhost:5432/unispace`
- `DATABASE_USERNAME`, defaulting to `unispace`
- `DATABASE_PASSWORD`, defaulting to `unispace`
- `SPRING_PROFILES_ACTIVE=demo` when the optional demo seed is wanted

The checked-in `.env.example` documents these settings together with the existing CORS and JWT settings. There is no dual-database mode and no MongoDB connection setting remains in production code.

## Verification

- Backend Maven suite: 20 passed, 0 failed.
- The Spring application context test passes against H2 in PostgreSQL compatibility mode; Flyway is disabled only for this test profile while Hibernate creates the disposable test schema.
- A packaged backend was started against a clean PostgreSQL 17.10 container.
- Flyway successfully created and applied schema version 1.
- Hibernate successfully validated the migrated schema with `ddl-auto=validate` and the application reached a healthy started state.
- A direct database test attempted two overlapping approved bookings in one rolled-back transaction; PostgreSQL rejected the second insert through `bookings_no_approved_overlap`.
- A second startup with the `demo` profile created `admin@iut-dhaka.edu` with the `ADMIN` role and a profile linked to the same UUID.
- A production-source scan found no remaining MongoDB, `MongoRepository`, or `ObjectId` references.
- Frontend production build passes. Its existing bundle-size and stale Browserslist-data advisories remain non-blocking.

## Compatibility and migration boundary

This phase supports a fresh PostgreSQL database. Existing MongoDB documents are not copied automatically. Phase 5 remains optional for this academic project: it is needed only if existing users, classrooms, routines, bookings, or role requests must be retained.

MongoDB and PostgreSQL identifiers are different types, so a Phase 5 migration must create a stable old-ID-to-new-UUID mapping and use it consistently for every foreign-key relationship. Password hashes can be retained as stored values; legacy null or inconsistent profile links must be repaired or reported during migration.

If existing data is not required, Phase 5 can be treated as a documented no-data migration decision and the demo profile can initialize a usable administrator on the fresh database.

## Deferred to later phases

- Optional export, transformation, reconciliation, and import of existing MongoDB records (Phase 5).
- Dockerfiles, Compose networking, PostgreSQL volume configuration, startup health checks, and container-oriented environment defaults (Phase 6).
- End-to-end deployment verification and release-readiness checks (Phase 7).
