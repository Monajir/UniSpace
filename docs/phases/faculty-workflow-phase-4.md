# Faculty workflow improvements — Phase 4

## Status

Complete on 2026-09-12.

## Scope

This phase replaced transient, booking-derived messages with persistent, recipient-specific notifications stored in PostgreSQL. Notifications now have durable read state and event timestamps.

## Implemented

- Added a `notifications` table through Flyway migration `V2__create_notifications.sql`.
- Linked notifications to their recipient and originating booking, with cascade behavior for deleted users and preserved messages when a booking is removed.
- Added four event types: booking submitted, assigned to faculty, approved, and rejected.
- Created notifications transactionally with booking state changes so a decision and its notification cannot diverge.
- Made event creation idempotent per recipient, booking, and event type.
- Added authenticated APIs to list personal notifications, mark one as read, and mark all as read.
- Enforced notification ownership in repository queries; another user's notification cannot be read or modified by identifier.
- Added one reusable frontend notification feed with unread counts, timestamps, individual read actions, and a mark-all-read action.
- Replaced the student/CR profile's generated messages with the persistent feed and added the same feed to the faculty dashboard.
- Backfilled notifications for bookings already present when the Flyway migration runs.
- Seeded 11 connected demo notifications without duplicating them on restart.

## Notification recipients

- The requester receives submission and eventual approval or rejection events.
- A faculty user receives an assignment event when a booking names their institutional email and that account has the `FACULTY` role.
- Administrators continue to use the administrator request queue and are not sent duplicate per-request notifications.

## Verification

- Backend test suite: 57 tests passed with zero failures or errors.
- Notification-focused tests cover recipient isolation, ownership checks, read state, requester/faculty delivery, and duplicate prevention.
- Demo initialization verifies 11 connected notifications and restart idempotence.
- `npm run typecheck`: passed.
- `npm run lint`: passed with zero errors and eight existing Fast Refresh warnings.
- `npm run build`: passed. Existing bundle-size and Browserslist-data advisories remain.

## Operational behavior

Existing PostgreSQL volumes are upgraded automatically by Flyway the next time the rebuilt backend starts. Read/unread changes persist across container restarts because they are stored in the PostgreSQL volume.

The client refreshes notifications when the feed opens and whenever its browser window regains focus. Push delivery or background polling is not included because it is unnecessary for the academic demonstration.
