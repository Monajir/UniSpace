# Administrator Classroom Update and Deletion

**Status:** Complete

**Completed:** 2026-09-20

## Objective

Complete classroom management by allowing administrators to update classroom details and safely delete unused classrooms from the Admin Dashboard.

## Backend

- Added admin-only `PATCH /api/classrooms/{id}` for room number, building, capacity, and equipment updates.
- Added admin-only `DELETE /api/classrooms/{id}` for permanent classroom deletion.
- Reuses classroom normalization, field validation, and case-insensitive duplicate-room detection for both creation and updates.
- Update and deletion obtain the classroom's pessimistic database lock, serializing these operations with concurrent booking requests.
- Deletion is rejected with HTTP 409 when booking or routine records reference the classroom, preserving historical and connected academic data.
- Successful updates and deletions use the existing post-commit Redis catalogue invalidation.

## Frontend

- Added Edit and Delete actions to each classroom card.
- Editing reuses the classroom dialog with existing values prefilled.
- Deletion requires explicit confirmation and explains the dependency restriction.
- Successful operations refresh the classroom catalogue and show confirmation notifications.
- Backend validation and dependency errors are shown to the administrator.

## Data Compatibility

No database migration or existing-data rewrite is required. Existing foreign-key restrictions remain in place as a database-level safety net.

## Verification

- Complete backend suite: 72 tests passed with no failures or errors.
- Added application-level coverage for update, deletion, administrator authorization, and deletion rejection for a classroom with routine history.
- Frontend TypeScript checking passed.
- Frontend lint passed with 0 errors and the same 8 existing Fast Refresh warnings.
- Frontend production build passed; the existing Browserslist-age and bundle-size advisories remain non-blocking.
