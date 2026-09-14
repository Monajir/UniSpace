# Phase 7 — Verification and release readiness

Status: Complete  
Completed: 2026-08-29

## Objective

Turn the PostgreSQL/Docker migration into a repeatable, evidence-backed academic release by testing a fresh deployment, reviewing dependencies and images, fixing release blockers, and recording the remaining production decisions.

## Implemented changes

### Automated clean-stack verification

- Added `scripts/release-check.ps1` as an isolated, disposable release gate.
- The gate refreshes build base images and provisions a new PostgreSQL volume on every run.
- Added 22 end-to-end assertions for health, proxying, routing, authentication, authorization, user creation, PostgreSQL classroom data, booking lifecycle rules, ownership, conflicts, public schedules, and role approval.
- The release stack uses dedicated ports and project naming, so it does not mutate the normal UniSpace database.
- Cleanup runs even when an assertion fails; `-KeepStack` is available for investigation.

### Dependency and image remediation

- Updated the frontend lockfile to current safe versions allowed by its dependency ranges.
- Upgraded React Router DOM from 6.x to 7.18.3 to resolve the remaining production-tree redirect and hydration advisories.
- Updated TypeScript ESLint integration after the security refresh exposed an ESLint compatibility failure.
- Replaced deprecated empty interfaces, removed explicit `any` usage in active profile/auth code, and converted the Tailwind plugin to an ES module import so lint succeeds.
- Upgraded the unprivileged Nginx runtime from 1.27 Alpine to 1.29 Alpine after the original image scan found six critical and 27 high operating-system vulnerabilities.
- Added base-image refreshes to the release script to avoid silently reusing vulnerable mutable-tag layers.

### Runtime hardening

- Enabled `no-new-privileges` for backend and frontend containers.
- Added bounded JSON log rotation to all services (10 MB per file, three files).
- Kept the PostgreSQL-only network internal and the database unpublished.

### Documentation

- Added the release-readiness guide with commands, scope, security decisions, release checklist, and internet-facing production prerequisites.
- Updated the phase index and Docker deployment operations.

## Verification evidence

- Backend Maven suite: 27 passed, 0 failed, 0 errors, 0 skipped.
- Frontend lint: 0 errors; 11 non-blocking fast-refresh/hook warnings.
- Frontend production build: passed with React Router 7.
- Frontend production dependency audit: 0 vulnerabilities.
- Docker Scout backend runtime: 0 high or critical findings.
- Docker Scout frontend runtime after Nginx upgrade: 0 high or critical findings.
- Fresh Compose/PostgreSQL release gate: 22 assertions passed.
- Browser verification against the deployed React Router 7 image showed the seeded classroom and schedule route with no console errors.
- Compose configuration validation passed.

The browser test used `http://localhost:18080` because port 8080 was occupied on the test host.

## Release conclusion

The project is feasible and ready for its intended academic/demo deployment. The main requested modernization goals—Dockerized application delivery and PostgreSQL persistence—are complete and verified together from an empty environment.

It is not yet suitable for an untrusted public production environment. TLS, managed secrets, resource sizing, centralized observability, abuse protection, and a formal backup/restore operation remain deployment responsibilities and are detailed in the release-readiness guide.

Public signup is intentionally not part of this release. Users are provisioned by the demo administrator, consistent with the agreed academic scope.

