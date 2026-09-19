# UniSpace release-readiness guide

## Release classification

UniSpace is ready for an academic demonstration or a trusted local deployment. It is not approved as an internet-facing production service without the additional controls listed below.

## Automated release gate

From the repository root, run:

```powershell
./scripts/release-check.ps1
```

The script uses the isolated Compose project `unispace-release-check`, defaults to frontend port `18080` and backend port `18082`, and always starts with a new PostgreSQL volume. It refreshes base images, builds both application images, waits for all health checks, seeds one test classroom, and verifies 22 assertions covering:

- frontend, backend, and PostgreSQL readiness;
- Nginx-to-backend proxying and SPA deep links;
- anonymous access restrictions and administrator login;
- administrator-created student and CR accounts;
- classroom data persisted in PostgreSQL;
- CR-only booking creation;
- booking approval, rejection, cancellation, ownership, overlap prevention, and public schedules;
- student role requests and administrator approval.

All release-check containers, networks, and the temporary database volume are removed in a `finally` block. Use `-KeepStack` only for investigation or browser testing, and clean it afterward with:

```powershell
docker compose --project-name unispace-release-check down --volumes --remove-orphans
```

Use `-SkipBuild` only during local troubleshooting. It is not a release-quality run.

## Additional release commands

```powershell
cd Backend/UniSpace
mvn test

cd ../../Frontend/classflo-booking
npm run lint
npm run build
npm audit --omit=dev
```

Docker Scout was used for the Phase 7 image review:

```powershell
docker scout cves --only-severity critical,high local://unispace-release-check-backend:latest
docker scout cves --only-severity critical,high local://unispace-release-check-frontend:latest
```

Refresh image layers for later reviews; a cached image can retain operating-system vulnerabilities even when a mutable tag has been rebuilt upstream.

## Security and operating decisions

- The final frontend dependency tree has zero production `npm audit` findings. Development-only Vite tooling is not copied into the runtime image.
- Both final application images have zero high or critical Docker Scout findings as of 2026-08-29.
- Application containers run as non-root users with `no-new-privileges` enabled.
- Container logs rotate at 10 MB with three retained files.
- PostgreSQL remains on an internal Docker network and has no host port.
- Only Actuator health is publicly exposed; component details are hidden.
- Demo-mode credentials and a repository-visible JWT default are accepted only for this academic scope.
- Public signup remains intentionally unavailable. The demo administrator provisions users through the administrator interface/API.

## Required before internet-facing production

1. Terminate TLS at a maintained reverse proxy or platform load balancer.
2. Supply database, JWT, and administrator credentials through a managed secret store; disable demo defaults.
3. Pin reviewed image digests and establish a scheduled dependency/image refresh process.
4. Add deployment-specific CPU and memory limits after measuring the target host.
5. Add centralized logs, metrics, alerting, database backups, and a tested restore procedure.
6. Add rate limiting, account recovery, login abuse controls, and a decision on public registration.
7. Split the approximately 543 KB frontend JavaScript bundle if low-bandwidth performance matters.

## Release checklist

- Start from the intended commit and confirm no unreviewed local configuration is present.
- Run the backend tests, frontend lint/build, production dependency audit, and automated release gate.
- Scan the newly built runtime images, not stale local tags.
- Verify backup and restore on the target environment before importing real data.
- Record image digests, configuration source, Flyway schema version, and release-check result for the deployment.
