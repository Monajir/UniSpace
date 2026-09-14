# Phase 3 — Booking and account integrity

Status: Complete  
Completed: 2026-08-28

## Objective

Make booking, account, profile, and role mutations obey explicit consistency rules before replacing MongoDB with PostgreSQL.

## Implemented changes

### Booking validation

- The authenticated user is resolved before a booking is created; missing accounts fail explicitly.
- The classroom must exist and be marked available.
- Booking dates cannot be in the past.
- Start time must be before end time.
- Duration cannot exceed the published limit of 1 hour 15 minutes.
- A non-empty reason and a valid `@iut-dhaka.edu` faculty email are required.
- Faculty email addresses are normalized to lowercase.
- The booking day is derived from the submitted date instead of trusting client-supplied text.
- New requests are checked against recurring classroom routines and already approved bookings.
- Adjacent intervals are allowed; intervals only conflict when they actually overlap.

### Approval and rejection integrity

- Approval revalidates classroom availability, date, duration, routines, and approved bookings so conditions cannot become stale while a request waits.
- Overlapping approvals are serialized within the running backend process. A concurrency regression test confirms that two overlapping requests produce exactly one approved winner.
- Only pending requests may be approved or rejected; repeated or conflicting state transitions return `409 Conflict`.
- Rejection now records the `rejected` state instead of deleting the request, preserving an audit trail.
- Classroom schedules expose only approved bookings as occupied time.
- Faculty approval queues expose only pending requests.
- Added a compound MongoDB lookup index definition for classroom, booking date, and status.

### Account and profile consistency

- User validation and duplicate-email checks now live in `UserService`, not in the controller.
- Email addresses are normalized before persistence.
- New users are saved before their profile, ensuring MongoDB has generated the user ID used by `profile.user_id`.
- If profile creation fails, the newly created user is removed as a compensating action.
- Duplicate-key races return `409 Conflict` instead of an internal server error.
- Profile role selection has an explicit priority: `ADMIN`, `FACULTY`, `CR`, then `STUDENT`.
- Account deletion removes the user's bookings, pending role request, and profile before deleting the user.
- Removed incorrect uniqueness declarations from human names; different users may share the same name.

### Role consistency

- Role requests are restricted to supported `CR` and `STUDENT` transitions.
- Duplicate pending requests for the same account are rejected.
- Missing role requests, users, and profiles fail explicitly instead of relying on disabled Java assertions or silently returning.
- Approving a role updates both the user's role list and profile role.
- Existing profiles with a missing `user_id` are repaired during successful role approval.
- If profile synchronization fails, the service restores the user's original roles as compensation.

### API and frontend behavior

- Booking and role approval/rejection endpoints return `204 No Content` on success.
- User creation returns `201 Created` with the created safe user DTO.
- Validation and conflict failures use the structured API error format from Phase 1 with meaningful `400`, `404`, or `409` statuses.
- The frontend now displays backend conflict messages, including occupied-room and duplicate-role-request errors.
- User deletion no longer sends an unrelated create-user payload.

## Concurrency and transaction boundary

The current MongoDB connection is a standalone server, which does not support multi-document transactions. Phase 3 therefore uses ordered writes and explicit compensation for user/profile and role/profile changes. This prevents the known normal failure modes but cannot guarantee rollback if the process or machine stops between two MongoDB writes.

Booking approval serialization is process-local and is correct for the current single-backend academic deployment. Phase 4 must replace both mechanisms with PostgreSQL transactions and database-backed locking or constraints so integrity remains guaranteed across crashes and multiple backend instances.

## Verification

- Backend Maven suite: 20 passed, 0 failed.
- Tests cover owned cancellation, unauthorized cancellation, booking request normalization, maximum duration, past dates, routine overlap, status-filtered schedules, and parallel overlapping approvals.
- Tests cover generated user IDs reaching profiles, profile-creation compensation, dependent account deletion, role/profile synchronization, duplicate role requests, and role rollback after profile failure.
- Phase 1 security tests and Phase 2 API mapping tests continue to pass.
- Frontend TypeScript check passes.
- Frontend production build passes.
- The existing frontend bundle-size advisory remains non-blocking.

## Operational note for existing MongoDB data

Removing a uniqueness annotation does not remove an index already created in MongoDB. If the existing database contains unique indexes on `Profile.full_name` or `pending_roles.name`, those legacy indexes should be removed before creating two users with the same name. They are not part of the PostgreSQL schema planned for Phase 4.

## Deferred to later phases

- PostgreSQL entities, relationships, constraints, transactions, and schema migrations (Phase 4).
- Database-level protection for overlapping approvals across multiple application instances (Phase 4).
- Demo administrator seeding under an explicit development/demo profile (Phase 4).
- Optional conversion of existing MongoDB records, including any null legacy profile user IDs (Phase 5).
- Docker and Compose deployment (Phase 6).
