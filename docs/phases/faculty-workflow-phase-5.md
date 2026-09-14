# Faculty Workflow Phase 5 — Strict Booking Slots and Course Data

**Status:** Complete  
**Completed:** 2026-09-14

## Objective

Prevent booking requests from using arbitrary start and end times, and ensure every new request includes the course shown to reviewers in the pending-booking interface.

## Implementation

- Centralized the six published booking slots in the frontend:
  - 08:00–09:15
  - 09:15–10:30
  - 10:30–11:45
  - 11:45–13:00
  - 14:30–15:45
  - 15:45–17:00
- Reused the shared slot definition in the classroom schedule and booking form.
- Replaced editable date and time fields with a read-only summary of the schedule entry selected by the user.
- Added a required course field to the booking form.
- Added backend validation that rejects requests unless their start and end times exactly match a published slot. This also prevents direct API requests from bypassing the frontend restriction.
- Added backend validation requiring a nonblank course code/name, trimmed to a maximum of 100 characters.
- New pending-booking records now contain the actual course supplied during the request.

## Compatibility

No database migration was required because the booking table already contains the course field. Existing historical rows without course data remain valid and may continue to display `Not specified`; the stricter requirement applies to newly created requests.

## Verification

- Frontend type checking passed.
- Frontend lint passed with no errors; eight existing Fast Refresh warnings remain.
- Frontend production build passed.
- Backend test suite passed: 59 tests, 0 failures, 0 errors, 0 skipped.
- Added regression tests for rejecting arbitrary times and missing course data.

