# Phase 5 — Legacy-data decision

**Status:** Retired

**Originally completed:** 2026-08-29

**Retired:** 2026-09-19

## Decision

UniSpace uses PostgreSQL as its only application database and does not need to retain data from the earlier academic prototype. The optional MongoDB-export importer was therefore removed to reduce maintenance and presentation complexity.

Removed items include the legacy reader, transformer, validator, importer, command-line profile, test fixtures, tests, environment variables, and operating guide. No application data was deleted by this source-code cleanup.

## PostgreSQL boundary

The Flyway SQL files in `src/main/resources/db/migration` are unrelated to the retired importer and remain essential. They create and evolve the PostgreSQL schema whenever the application starts against a new or outdated database.

Fresh demonstration data continues to be created by the explicit `demo` profile. Existing data remains in the persistent PostgreSQL Docker volume across ordinary rebuilds and restarts.

## Verification

- Backend verification build passed: 61 tests, 0 failures, 0 errors.
- The packaged JAR contains no legacy migration classes or migration-profile properties.
- The packaged JAR retains both PostgreSQL Flyway scripts: `V1__create_postgresql_schema.sql` and `V2__create_notifications.sql`.
- Docker Compose configuration validation passed.
- Runtime source, environment examples, scripts, and current operational documentation contain no legacy migration-profile or MongoDB references.
