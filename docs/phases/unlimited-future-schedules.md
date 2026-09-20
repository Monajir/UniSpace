# Unlimited Future-Week Schedules

**Status:** Complete

**Completed:** 2026-09-20

## Objective

Remove the two-week browsing limit while retaining the existing weekly routine model, strict booking slots, conflict checks, and stored PostgreSQL data.

## Implemented Behavior

- The classroom schedule page can move forward through any number of weeks.
- Each selected week is fetched with `GET /api/classrooms/{id}/schedule?weekStart=YYYY-MM-DD`.
- The backend normalizes the supplied date to Monday and returns bookings through Friday.
- Omitting `weekStart` still returns the current week.
- Routine entries continue to repeat by weekday and time in every displayed week.
- Pending and approved bookings are displayed only on their exact stored date.
- Past days in the current week remain unavailable, and navigation before the current week remains disabled.
- Stale responses are ignored if a user changes weeks before an earlier request finishes.
- Schedule cells remain non-bookable while a week is loading or if its request fails, avoiding decisions based on incomplete data.
- Invalid date parameters return HTTP 400 instead of an internal-server error.

## Data Compatibility

No database migration, data reset, or record rewrite is required. Existing bookings already store an exact booking date, and existing routines store a weekday and time range, so both models work unchanged for later weeks.

## Performance

The backend now queries only the selected Monday-to-Friday date range and the relevant calendar statuses instead of loading every booking for the classroom and filtering the collection in application memory.

## Verification

- Added service coverage for a week eight weeks in the future and Monday normalization.
- Added application-level coverage for an arbitrary future date and invalid date input.
- Complete backend suite: 66 tests passed with no failures or errors.
- Frontend TypeScript checking passed.
- Frontend lint passed with 0 errors and the same 8 existing Fast Refresh warnings.
- Frontend production build passed; the existing Browserslist-age and bundle-size advisories remain non-blocking.
