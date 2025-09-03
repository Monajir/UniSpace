UniSpace — Classroom Booking & Scheduling

Automated classroom booking that prevents clashes and delivers instant, trustworthy confirmations for CRs, Faculty, and Admins.

Frontend: TypeScript (React) — Frontend/classflo-booking

Backend: Java (Spring Boot + JWT) — Backend/UniSpace

Core idea: Request → Validate → Check Availability → Finalize → Receipt/Notify

Goals: Conflict rate < 2%, approval SLA < 60 min, auditable schedule



---

✨ Features

Authentication & Roles: CR / Faculty / Admin (JWT)

New Booking: guided form, field validation, room/time hints

Clash Detection: overlap vs routine; maintenance blocks

Approvals & Overrides: reason required; full audit trail

Rescheduling: edit request → re-validate → notify stakeholders

User Views: Faculty “My Schedule — Today/Week”

Admin Views: Exception Report (clashes/overrides), Utilization KPIs

Receipts/Notifications: mobile-friendly booking confirmation (QR-ready)

Decision Support: Weekly utilization heatmap (weekday × 75-min slot; quiz break 13:00–14:30)



---

🏗️ Architecture (High level)

Frontend (React+TS)
  └── Auth (JWT) • Booking UI • Schedules • Admin reports
       ↕ REST/JSON
Backend (Spring Boot)
  ├── Auth (JWT, filters/utils)
  ├── Booking Service (validation, availability, finalization)
  ├── Conflict & Maintenance checks
  ├── Approval workflow + logs
  └── Repositories (Rooms, Routine, Bookings, Approvals, Users)
       ↕
Database (RDBMS via JPA/Hibernate)

System of Record (SSOT): Classroom_Master, Routine_DB, Booking_Records, Approval_Logs (all outputs/read models derive from these).


---

⚙️ Tech Stack

Frontend: React + TypeScript, fetch/axios, componentized UI

Backend: Spring Boot, Spring Security (JWT), JPA/Hibernate

Database: PostgreSQL/MySQL (configurable)

Build/Tooling: npm/yarn, Maven/Gradle



---

🚀 Quick Start

Prerequisites

Node.js LTS (≥18), Java 17+, PostgreSQL/MySQL, Git


Backend (Spring Boot)

cd Backend/UniSpace
# create application.properties (or use env vars)
# spring.datasource.url=jdbc:postgresql://localhost:5432/unispace
# spring.datasource.username=...
# spring.datasource.password=...
# jwt.secret=replace-with-strong-secret
./mvnw spring-boot:run
# runs on http://localhost:8080

Frontend (React + TS)

cd Frontend/classflo-booking
cp .env.example .env
# VITE_API_BASE_URL=http://localhost:8080/api
npm install
npm run dev
# http://localhost:5173 (or shown port)


---

🔐 Configuration

Backend env (examples):

SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD

JWT_SECRET, SERVER_PORT (default 8080)


Frontend env (examples):

VITE_API_BASE_URL — e.g., http://localhost:8080/api



---

📡 API (snapshot)

POST /api/auth/login — JWT login

GET /api/rooms — list/search rooms

POST /api/bookings — submit booking request

GET /api/bookings?me=true — my bookings

GET /api/conflicts — conflicts for admin

POST /api/approvals/{id}/approve|reject|override — admin actions


> See inline controller/service docs for full parameters and response formats.




---

🗃️ Data Model (simplified)

User(id, name, email, role)

Room(id, building, code, capacity, active)

Routine(id, room_id, weekday, slot_start, slot_end, course/owner)

Booking(id, requester_id, room_id, date, start, end, purpose, status)

ApprovalLog(id, booking_id, approver_id, decision, reason, timestamp)

MaintenanceWindow(id, room_id, start, end, note)


Integrity: uniqueness on (room_id, date, start, end) to prevent double bookings.


---

🧭 UX & Accessibility

Clarity: card-based layout, strong hierarchy (time → room → status)

Semantics: color tags (Available/Pending/Rejected), explicit microcopy

Accessibility: contrast ≥ 4.5:1, keyboardable tables, labels over icons

Responsiveness: mobile-first views (e.g., booking receipt), horizontal scroll for wide tables



---

📊 KPIs & Visualization

Avg Utilization%, Conflict Rate%, Approval Lead-Time (min), Override Rate%

Weekly Utilization Heatmap: weekday × 75-min slots; quiz break 13:00–14:30 marked



---

🧪 Testing & Quality (recommended)

Frontend: component/unit tests; lint & type-check CI

Backend: service/controller tests; data integrity tests

CI (GitHub Actions): build, lint, tests on PR; coverage target ≥ 80%



---

🧭 Version Control & Workflow

Repo: https://github.com/Monajir/UniSpace

Branches: main (protected), short-lived feature/* → PR to main

Commits: Conventional Commits (e.g., feat(request): add overlap check)

PRs: small diffs, reviewer approval, status checks green; squash merge

Issues/Projects: labeled issues + Kanban (Backlog → In Progress → Review → Done)


Useful links:

Commits: https://github.com/Monajir/UniSpace/commits/main

Pull Requests: https://github.com/Monajir/UniSpace/pulls

Contributors: https://github.com/Monajir/UniSpace/graphs/contributors



---

🗺️ Roadmap

Email/push digest & SLA dashboard

ICS/Calendar export

SSO integration (institutional)

QR/door-lock integration (hardware API)

Bulk/cascade rescheduling with dependency graph



---

📄 License

Specify your license (e.g., MIT) in LICENSE. If unspecified, all rights reserved by the authors.


---

🤝 Contributing

1. Create an issue; discuss scope & acceptance criteria.


2. Branch: feature/short-slug


3. Commit using Conventional Commits; add tests when relevant.


4. Open a PR with screenshots (UI changes) and “why” details.


5. Address review comments; squash merge after approval.




---

🙏 Acknowledgments

Built for IUT coursework; thanks to mentors and peers for feedback on scheduling, UX, and structured analysis (DFDs, Data Dictionary, and process specs).
