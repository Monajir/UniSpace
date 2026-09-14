# Phase 5 — Migrate existing data

Status: Complete  
Completed: 2026-08-29

## Objective

Provide a safe, optional path for converting legacy UniSpace MongoDB documents into the PostgreSQL schema introduced in Phase 4 without adding MongoDB back to the running application.

## Implemented changes

### Export-file migration boundary

- Added a dedicated `migration` Spring profile that runs as a command-line process with no HTTP server or scheduled cleanup task.
- The command consumes files produced by `mongoexport`; it does not connect to or modify MongoDB.
- Both JSON arrays and newline-delimited JSON are supported.
- MongoDB Extended JSON values including `$oid`, `$date`, `$numberInt`, and `$numberLong` are normalized.
- Singular and plural legacy file-name aliases are accepted, while ambiguous duplicate aliases are rejected.
- Production remains a single-database PostgreSQL application.

### Deterministic conversion

- Every legacy document ID maps deterministically to a UUID using the collection type and MongoDB ID.
- References from profiles, routines, bookings, and pending role requests resolve through those maps.
- The generated report includes the complete old-ID-to-new-UUID map for reconciliation and audit.
- Existing password hashes are copied exactly, avoiding accidental double hashing.
- Emails, roles, booking statuses, weekdays, numeric values, dates, and times are normalized to the Phase 4 domain types.
- Missing or stale profile user references are repaired by a unique normalized-email match and recorded as warnings.

### Validation and reconciliation

- The command reports errors with collection, legacy ID, field, and explanation.
- It validates required values, supported roles/statuses, unique business keys, positive capacities, time ordering, booking duration, and all cross-document references.
- It rejects orphaned profiles, routines, bookings, and pending role requests.
- It rejects overlapping approved bookings and approved bookings that collide with recurring routines before PostgreSQL import.
- An empty or incorrectly selected export directory fails instead of reporting a misleading successful no-op.
- Source counts, converted target counts, repair warnings, and UUID mappings are written to a JSON report in both dry-run and import modes.

### Transaction and retry safety

- Dry run is the default and writes no application records.
- Actual import uses dependency-ordered JDBC inserts within one Spring transaction.
- Any validation or database failure rolls back the whole import.
- PostgreSQL must be empty before import. The guard is mandatory and cannot be disabled.
- Deterministic UUIDs make repeated validation reports stable; a completed import cannot accidentally be repeated or merged.

## Operation

The complete export, dry-run, correction, import, and reconciliation procedure is documented in the [MongoDB to PostgreSQL migration guide](../migration/mongodb-to-postgresql.md).

For this academic project, migration remains optional operationally. If legacy records are not needed, use a fresh PostgreSQL database and the Phase 4 demo administrator. The Phase 5 tooling is available if sample or historical data must be retained later.

## Verification

- Backend Maven suite: 27 passed, 0 failed.
- Tests cover deterministic IDs, Extended JSON, JSON arrays, newline-delimited exports, profile-link repair, orphan references, approved-booking overlaps, empty exports, and ambiguous filenames.
- The command was packaged and run against PostgreSQL 17.10.
- A dry run converted six fixture documents, emitted a UUID mapping and repair warning, and left all six application tables empty.
- A full import committed one user, profile, classroom, routine, booking, and pending role request.
- Direct PostgreSQL checks confirmed the profile/user, booking/user, and booking/classroom UUID relationships.
- The fixture's existing BCrypt password hash was preserved byte-for-byte.
- A second import was rejected by the mandatory empty-target guard and left all counts unchanged.
- Earlier failed integration attempts left zero rows, confirming transactional rollback.

## Deferred to later phases

- Containerizing the backend, frontend, and PostgreSQL services (Phase 6).
- Compose startup ordering, persistent database volumes, and health checks (Phase 6).
- End-to-end container deployment and release-readiness verification (Phase 7).
