# MongoDB to PostgreSQL migration guide

This command is an optional one-time bridge for retaining legacy UniSpace academic data. The running application remains PostgreSQL-only; it never connects to MongoDB.

## Safety model

- Exported JSON is read without changing MongoDB.
- Dry-run mode is the default and writes no application rows to PostgreSQL.
- Every MongoDB ID maps to a deterministic UUID and the complete mapping is written to the report.
- Validation errors stop the migration before import.
- Import is one PostgreSQL transaction: either all records commit or none do.
- Import always requires an empty PostgreSQL application schema. This guard cannot be disabled.
- The `demo` profile must not be active during migration because its administrator would make the target non-empty.

Keep backups of both databases and retain the generated migration report with the project records.

## 1. Export MongoDB collections

Use `mongoexport` to write one file per collection. Both newline-delimited JSON (the default) and `--jsonArray` output are accepted. MongoDB Extended JSON wrappers such as `$oid`, `$date`, and `$numberInt` are supported.

The reader accepts these file names:

| Data | Accepted file names |
|---|---|
| Users | `users.json` or `user.json` |
| Profiles | `profiles.json` or `profile.json` |
| Classrooms | `classrooms.json` or `classroom.json` |
| Routines | `routines.json` or `routine.json` |
| Bookings | `bookings.json` or `booking.json` |
| Pending roles | `pending_roles.json`, `pendingRole.json`, or `pendingrole.json` |

Spring Data MongoDB commonly used the singular class-derived names in the old project. Check the actual names first with `db.getCollectionNames()`. Example exports are:

```powershell
mongoexport --uri="mongodb://localhost:27017/UniSpace" --collection=user --out=users.json
mongoexport --uri="mongodb://localhost:27017/UniSpace" --collection=profile --out=profiles.json
mongoexport --uri="mongodb://localhost:27017/UniSpace" --collection=classroom --out=classrooms.json
mongoexport --uri="mongodb://localhost:27017/UniSpace" --collection=routine --out=routines.json
mongoexport --uri="mongodb://localhost:27017/UniSpace" --collection=booking --out=bookings.json
mongoexport --uri="mongodb://localhost:27017/UniSpace" --collection=pendingRole --out=pending_roles.json
```

Do not place two aliases for the same data type in the input directory; the validator rejects ambiguous files.

## 2. Prepare an empty PostgreSQL target

Create the PostgreSQL database and configure the same `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` values used by UniSpace. Flyway creates schema version 1 when the command starts.

The target must contain no users, profiles, classrooms, routines, bookings, or pending role requests. Running a demo seed before migration will intentionally trigger the empty-target guard.

## 3. Build and dry-run

From `Backend/UniSpace`, build the application and configure the migration input:

```powershell
.\mvnw.cmd package
$env:DATABASE_URL = "jdbc:postgresql://localhost:5432/unispace"
$env:DATABASE_USERNAME = "unispace"
$env:DATABASE_PASSWORD = "unispace"
$env:MIGRATION_INPUT_DIR = "D:\exports\unispace"
$env:MIGRATION_REPORT_FILE = "D:\exports\unispace\migration-report.json"
$env:MIGRATION_DRY_RUN = "true"
java -jar target\UniSpace-0.0.1-SNAPSHOT.jar --spring.profiles.active=migration
```

Successful validation produces status `DRY_RUN_VALID`. Review every warning and compare `sourceCounts` with `targetCounts`. The report also contains the complete legacy-ID-to-UUID mapping.

## 4. Correct validation failures

Status `VALIDATION_FAILED` means no application rows were imported. Each issue identifies the collection, legacy ID, field, and reason. The validator checks, among other rules:

- required values, email formats, roles, booking statuses, dates, times, and capacity;
- duplicate emails, room numbers, profiles, IDs, and pending requests;
- user/classroom references from profiles, routines, bookings, and role requests;
- the 75-minute booking limit;
- overlapping approved bookings and approved bookings that overlap recurring routines.

A missing or stale profile `user_id` is repaired automatically when exactly one migrated user has the same normalized email. That repair appears as a warning. Invalid records are never silently dropped from an otherwise successful import.

## 5. Import

After a clean dry run, change only the mode:

```powershell
$env:MIGRATION_DRY_RUN = "false"
java -jar target\UniSpace-0.0.1-SNAPSHOT.jar --spring.profiles.active=migration
```

Success produces status `IMPORTED`. Password hashes are copied exactly and are not encoded a second time. UUID foreign keys are generated from the report mapping and inserted in one transaction.

An accidental second import fails because the target is no longer empty. Start over with a new empty database if the source export must be corrected after a successful import.

## 6. Reconcile and start UniSpace

Compare the report counts with direct PostgreSQL counts, retain the report, and then start the normal application profile. If the migrated data has no administrator, the optional `demo` profile can create the configured demo administrator after migration.

The migration profile disables the HTTP server, security filter chain, and scheduled cleanup job. It exits when validation or import finishes and must not be used to run the application.
