# Faculty workflow improvements — Phase 2

## Status

Complete on 2026-09-12.

## Scope

This phase made pending bookings visible and reliable in the classroom calendars. It also defined one consistent reservation rule for pending requests and synchronized schedule data after booking status changes.

## Implemented

- Included both `PENDING` and `BOOKED` requests in the current-week and next-week classroom schedule responses. Rejected requests remain excluded.
- Kept the existing calendar presentation: approved bookings use the booked style and pending requests use the amber pending style.
- Made pending requests temporarily reserve their requested time. A new request that overlaps either a pending or approved booking is rejected by the backend, with room-level locking to keep simultaneous submissions consistent.
- Preserved approval of legacy competing pending requests by checking an approval against approved bookings only. Once one is approved, approval of an overlapping request is rejected.
- Refreshed schedule data after a request is created, approved, or rejected.
- Added same-tab, cross-tab, and window-focus schedule synchronization so calendars do not require a full page reload.
- Kept adjacent bookings valid: a request ending exactly when another starts is not considered an overlap.

## Verification

- Backend test suite: 46 tests passed, including current-week and next-week pending visibility, pending-slot conflicts, and adjacent-slot handling.
- `npm run typecheck`: passed.
- `npm run lint`: passed with zero errors and eight existing Fast Refresh warnings.
- `npm run build`: passed. The existing bundle-size and Browserslist-data advisories remain.

## Deferred to later phases

- Secure department-scoped faculty approval and rejection endpoints.
- A useful faculty dashboard based on those endpoints.
- Persistent notification records with read/unread state and event timestamps.
