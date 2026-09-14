# Faculty workflow improvements — Phase 1

## Status

Complete on 2026-09-12.

## Scope

This phase addressed the immediate frontend usability and data-display issues identified during the faculty workflow review. It intentionally did not add faculty approval APIs or change which booking statuses are returned by classroom schedule endpoints.

## Implemented

- Replaced the shared hard-coded notifications with booking updates derived from the authenticated student or CR user's actual booking records.
- Added a truthful empty notification state for faculty accounts until the faculty approval workflow is implemented.
- Added `cursor-pointer` to available schedule cells for CR users. Read-only and unavailable cells retain non-clickable cursors.
- Added `CR` to administrator user creation. Selecting CR automatically selects STUDENT; removing STUDENT also removes CR so the role combination stays valid.
- Added a destructive confirmation dialog before user deletion and success/error feedback after the request completes.
- Expanded pending-booking cards with classroom, requester, status, date, full time range, course, faculty name/email, creation date, and reason.
- Added success feedback after user creation.

## Verification

- `npm run typecheck`: passed.
- `npm run lint`: passed with zero errors and eight existing Fast Refresh warnings.
- `npm run build`: passed. The existing bundle-size and Browserslist-data advisories remain.

## Deferred to later phases

- Secure faculty-owned approval and rejection endpoints and a dedicated faculty dashboard.
- Showing pending bookings in classroom calendars and defining whether pending requests reserve a slot.
- Persistent notification records, read/unread state, and event timestamps.
