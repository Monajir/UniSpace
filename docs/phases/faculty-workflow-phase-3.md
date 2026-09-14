# Faculty workflow improvements — Phase 3

## Status

Complete on 2026-09-12.

## Scope

This phase turned the faculty role into an active booking reviewer while preserving strict ownership boundaries. The existing data model has no department relationship shared by faculty, bookings, classrooms, and courses, so authorization is based on the booking's assigned faculty email rather than an inferred department.

## Implemented

- Added authenticated faculty endpoints for assigned booking history, approval, and rejection.
- Derived the faculty identity exclusively from the authenticated JWT principal. No email or faculty identifier supplied by the browser is trusted for authorization.
- Restricted the faculty endpoints to the `FACULTY` role while retaining the existing administrator-wide review endpoints.
- Enforced booking ownership again in the service layer. A faculty member receives a not-found response when attempting to change a request assigned to another faculty member.
- Reused room locking and schedule-conflict validation during faculty approval.
- Removed the old URL that accepted an arbitrary faculty email.
- Added a dedicated glassmorphic faculty dashboard with pending counts, approved/rejected totals, complete request details, confirmation dialogs, and decision history.
- Updated role-aware navigation so faculty accounts open the new dashboard; direct visits to the generic faculty profile redirect there as well.
- Refreshed classroom schedules immediately after a faculty decision.
- Updated the presentation guide to describe the faculty demonstration workflow.

## Verification

- Backend test suite: 52 tests passed with zero failures or errors.
- Faculty-focused booking and endpoint-security tests: 21 passed.
- `npm run typecheck`: passed.
- `npm run lint`: passed with zero errors and eight existing Fast Refresh warnings.
- `npm run build`: passed. Existing bundle-size and Browserslist-data advisories remain.

## Authorization model

A faculty account can review only bookings whose normalized `faculty_email` matches the authenticated account email. This is narrower and safer than treating the existing student `program` field as a department.

Department-wide approval remains feasible, but it requires an explicit department model and a database migration for faculty, courses or bookings, and possibly classrooms. That larger schema change is intentionally outside this phase.

## Deferred to later phases

- Persistent notifications with read/unread state and event timestamps.
- An explicit department domain model if department-wide review becomes a requirement.
