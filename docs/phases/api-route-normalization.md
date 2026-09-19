# API Route Normalization

**Status:** Complete

**Completed:** 2026-09-19

## Objective

Give the backend a consistent REST-style HTTP surface without changing its PostgreSQL entities, business rules, authentication format, response DTOs, or booking concurrency controls.

## Normalized Resource Groups

All application endpoints now use the `/api` namespace:

- `/api/auth` — login and current-session information
- `/api/classrooms` — available classrooms and room schedules
- `/api/bookings` — booking creation, administration, and assigned faculty reviews
- `/api/me` — the authenticated student's bookings and routine
- `/api/notifications` — personal notifications and read state
- `/api/role-requests` — role-request creation and administration
- `/api/users` — administrator-managed user collection

Important route changes:

| Previous route | Current route |
|---|---|
| `POST /public/login` | `POST /api/auth/login` |
| `POST /api/bookings/room/book` | `POST /api/bookings` |
| `GET /api/bookings/classSchedule/{id}` | `GET /api/classrooms/{id}/schedule?week=current` |
| `GET /api/bookings/classSchedule/next/{id}` | `GET /api/classrooms/{id}/schedule?week=next` |
| `GET /api/bookings/faculty` | `GET /api/bookings/assigned-to-me` |
| Faculty-specific approval/rejection paths | Shared `/api/bookings/{id}/approve` and `/reject` paths |
| `/student/my/bookings` | `/api/me/bookings` |
| `/student/my/routine` | `/api/me/routine` |
| `/student/role-request` and `/roles/pending` | `/api/role-requests` |
| `/roles/all` and `/roles/create/user` | `GET` and `POST /api/users` |
| `/roles/delete/{id}` | `DELETE /api/users/{id}` |
| `PATCH /api/notifications/read-all` | `PATCH /api/notifications` |

Approval and rejection remain explicit command endpoints. This is a deliberate pragmatic choice: it preserves clear authorization and transition-specific service methods while the rest of each path remains resource-oriented.

## Structural Cleanup

- Split the former mixed `RolesController` into `RoleRequestController` and `UserController`.
- Replaced the misleading `PublicController` with `LoginController`.
- Removed the ignored `available` classroom query parameter; `/api/classrooms` explicitly returns available classrooms.
- Removed the dormant signup client and commented signup backend code. Accounts remain administrator-managed, while students can request eligible role changes.
- Consolidated administrator and assigned-faculty booking decisions into the same route; the controller selects the correctly scoped service operation from the authenticated role.
- Corrected the internal `getUerByEmail` typo to `getUserByEmail`.
- Restricted the frontend reverse proxy to `/api/**` instead of retaining legacy top-level aliases.

## Compatibility

Legacy route aliases were intentionally not retained. The frontend and backend must therefore be rebuilt and deployed together. No database migration or data reset is required, and existing JWTs remain valid.

## Verification

- Backend suite: 62 tests, 0 failures, 0 errors.
- Added an application-level route contract test that confirms normalized mappings exist and legacy aliases do not.
- Frontend type checking passed.
- Frontend lint passed with 0 errors and the same 8 existing Fast Refresh warnings.
- Frontend production build passed; existing stale Browserslist-data and bundle-size advisories remain non-blocking.
- Docker Compose configuration validation passed.

