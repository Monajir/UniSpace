# Faculty Workflow Phase 6 — Registered Faculty Resolution

**Status:** Complete

**Completed:** 2026-09-19

## Problem

New booking requests derived `faculty_name` from the portion of `faculty_email` before `@`, while demo-seeded bookings supplied a polished name directly. Because the admin dashboard displays the stored booking value, equivalent records had inconsistent faculty names.

## Decision

A booking request must reference an existing UniSpace user with the `FACULTY` role. Rejecting unknown or non-faculty accounts is safer than retaining an email-prefix fallback because it:

- prevents requests from being assigned to invented or mistyped addresses;
- guarantees the assigned faculty can authenticate, review the request, and receive notifications;
- makes the user table the authoritative identity source;
- produces one simple rule to explain and test.

The normalized email remains the booking's assignment identifier. The faculty's current `User.name` is copied into the booking as a display snapshot when the request is created. A future schema could replace the email association with a faculty-user foreign key, but that migration is unnecessary for this academic scope.

## Implementation

- Validate the email syntax and normalize it to lowercase.
- Look up the corresponding user before locking or checking classroom availability.
- Require that user's role collection to contain `FACULTY`.
- Reject an unknown or non-faculty account with HTTP `400` and the message `Faculty email must belong to a registered faculty account`.
- Copy the registered user's name into `faculty_name`; the email-prefix fallback has been removed.
- Explain the registered-account requirement beside the booking-form field and in its guidelines.

Existing booking rows are not rewritten, so historical manually seeded names remain unchanged. No database migration is required.

## Verification

- Backend suite: 64 tests, 0 failures, 0 errors.
- Added tests for registered-name resolution, unknown faculty rejection, non-faculty rejection, and rejection before classroom locking.
- Frontend type checking passed.

