# College Student Election Voting System — Phase 1 (Foundation + Auth)

This is Phase 1 of the build: project skeleton, database entities for Student/Admin,
JWT-based Spring Security, and working register/login pages. Elections, positions,
candidates, and voting come in Phase 2+.

## What's included in this phase
- Maven project (Java 21, Spring Boot 3.3.4)
- `Student` and `Admin` entities (JPA, auto-create tables via `ddl-auto=update`)
- BCrypt password hashing, JWT auth, role-based access (`ROLE_STUDENT` / `ROLE_ADMIN`)
- `/api/auth/register` and `/api/auth/login` REST endpoints
- Thymeleaf pages: landing, login, register, and stub dashboards for both roles
- Global exception handler with clean JSON error responses
- A default admin account seeded on first startup (see below)

## 1. Install prerequisites
- **JDK 21** — https://adoptium.net
- **MySQL 8+** — https://dev.mysql.com/downloads/mysql/
- **IntelliJ IDEA** (Community or Ultimate) — https://www.jetbrains.com/idea/download

## 2. Create the MySQL database
You don't have to manually create the schema — `createDatabaseIfNotExist=true` in the
JDBC URL will create the `college_voting` database automatically on first run, as long
as your MySQL user has permission to create databases. Just make sure MySQL itself is
running and you know your root (or a dedicated) username/password.

## 3. Import into IntelliJ
1. Unzip this project.
2. In IntelliJ: **File → Open** → select the unzipped `college-voting-system` folder.
3. IntelliJ will detect the `pom.xml` and prompt to import as a Maven project — accept it.
4. Wait for Maven to download dependencies (bottom-right progress bar).

## 4. Configure environment variables
Open `src/main/resources/application-example.properties` for reference — do **not**
put real credentials directly into `application.properties`. Instead, set these as
environment variables (in IntelliJ: **Run → Edit Configurations → Environment variables**):

```
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
JWT_SECRET=replace-with-a-long-random-secret-at-least-32-characters
```

If you don't set them, the app falls back to `root` with no password and an insecure
default JWT secret — fine for quick local testing, but change `JWT_SECRET` before this
goes anywhere real.
<!-- Snake Game Repo View -->

<div align="center">
  <img src="https://profile-readme-generator.com/assets/snake.svg" alt="Snake animation" />
</div
## 5. Run it
Run `VotingApplication.java` (right-click → Run), or `mvn spring-boot:run` from a terminal.

The app starts on **http://localhost:8080**

## 6. Test the flow
- Visit `http://localhost:8080` → click **Register** → create a student account.
- Log in as that student → you'll land on a stub Student Dashboard confirming JWT auth works.
- Log in as the seeded admin to test the admin side:
  - **Email:** `admin@college.edu`
  - **Password:** `Admin@12345`
  - (Change these via `app.seed.admin-email` / `app.seed.admin-password` properties, or
    just change the password after logging in once Phase 2 adds a change-password endpoint.)

## Project structure
```
src/main/java/com/college/voting/
  entity/       Student, Admin, Role
  dto/          RegisterRequest, LoginRequest, LoginResponse, StudentResponse
  repository/   StudentRepository, AdminRepository
  security/     JwtUtil, JwtAuthFilter, CustomUserDetailsService
  config/       SecurityConfig, DataSeeder
  service/      AuthService
  controller/   AuthController (REST), PageController (Thymeleaf pages)
  exception/    GlobalExceptionHandler + typed exceptions (some pre-added for Phase 3)

src/main/resources/
  templates/    index, login, register, student/dashboard, admin/dashboard
  static/js/    auth.js (shared client-side JWT guard + fetch wrapper)
  application.properties, application-example.properties
```

## Why dashboard pages are "public" in SecurityConfig
The JWT is stored in browser `localStorage`, which is **not** automatically attached to
normal page navigation (only to `fetch()` calls we explicitly add the header to). So the
Thymeleaf page *shells* (`/student/**`, `/admin/**`) are publicly reachable, but each page
runs `requireAuth('ROLE_X')` from `auth.js` on load, which redirects to `/login` if there's
no valid token — and any real data those pages need will come from `/api/student/**` or
`/api/admin/**`, which **are** server-side protected and will reject requests without a
valid JWT for the correct role. This is the standard pattern for JWT + server-rendered
pages in the same app.

## Next: Phase 2 — DONE (included in this zip)
Elections, positions, and candidate management:
- Admin: create/update elections, start/close/cancel, add positions, add candidates
  (looked up by Student ID), approve/reject/disqualify candidates
- Student: browse active/upcoming/completed elections, view positions and approved
  candidates per election, read manifestos
- Pages: `/admin/elections`, `/student/elections`, `/student/election-details?id=X`

### New endpoints in this phase
```
POST   /api/admin/elections
PUT    /api/admin/elections/{id}
GET    /api/admin/elections
GET    /api/admin/elections/{id}
PATCH  /api/admin/elections/{id}/start
PATCH  /api/admin/elections/{id}/close
PATCH  /api/admin/elections/{id}/cancel

POST   /api/admin/elections/{electionId}/positions
PUT    /api/admin/elections/{electionId}/positions/{positionId}
DELETE /api/admin/elections/{electionId}/positions/{positionId}
GET    /api/admin/elections/{electionId}/positions

POST   /api/admin/candidates
PUT    /api/admin/candidates/{id}
GET    /api/admin/candidates/{id}
GET    /api/admin/candidates/election/{electionId}
PATCH  /api/admin/candidates/{id}/approve
PATCH  /api/admin/candidates/{id}/reject
PATCH  /api/admin/candidates/{id}/disqualify

GET    /api/student/elections/active
GET    /api/student/elections/upcoming
GET    /api/student/elections/completed
GET    /api/student/elections/{id}

GET    /api/student/candidates/election/{electionId}
GET    /api/student/candidates/position/{positionId}
GET    /api/student/candidates/{id}
```

**Note on election status:** `status` on the `Election` entity is the admin-set flag,
but every response uses `getEffectiveStatus()`, which independently checks the current
time against `startDateTime`/`endDateTime` (unless CANCELLED). This means an election
displays correctly as ACTIVE/COMPLETED even if an admin forgets to click Start/Close —
and it's this same effective-status logic that Phase 3's vote validation will rely on,
per the "backend must enforce start/end time, not just the frontend countdown" requirement.

**Note on candidate photos:** `photoUrl` is a plain string field for now (paste an image
URL). Real file upload (multipart → `/static/uploads`) is straightforward to add but
wasn't in this pass — flag it if you want it before Phase 3.

## To test Phase 2
1. Log in as admin (`admin@college.edu` / `Admin@12345`) → go to **Manage Elections** →
   create an election with a start time a few minutes from now.
2. Use the admin API directly (e.g. via Postman, or I can add a positions/candidates UI
   next) to add a position and a candidate — candidate creation needs an existing
   student's Student ID, so register a student first.
3. Approve the candidate via `PATCH /api/admin/candidates/{id}/approve`.
4. Log in as that (or any) student → **View Elections** → once the election's start time
   passes, it'll show as ACTIVE and the candidate will appear on the election details page.

## Next: Phase 3 — DONE (included in this zip)
The voting engine and results:
- **Ballot flow:** election details → select candidate per position → review/confirm →
  submit → success page (`/student/vote?electionId=X&positionId=Y` →
  `/student/vote-success`)
- **Every rule enforced server-side**, not just in the UI:
  - election must be ACTIVE right now (time-based `getEffectiveStatus()`, not just the
    stored flag — a forgotten "Close" click can't extend voting)
  - candidate must belong to the position and be APPROVED
  - one vote per student per position — checked in `VoteService` **and** backstopped by
    a DB unique constraint (`uk_one_vote_per_student_per_position`), so even two
    concurrent double-click requests can't both succeed
  - the authenticated student always comes from the JWT-derived SecurityContext
    principal — `VoteRequest` has no `studentId` field, so the frontend can never
    supply an identity
  - the Confirm button disables itself and shows "Submitting..." on click to prevent
    accidental double submission
- **Ballot privacy:** `VotingStatusResponse` only ever exposes *whether* a student voted
  for a position, never *who* they voted for. No endpoint anywhere returns another
  student's ballot choice.
- **Results:** calculated entirely server-side (`ResultService`) — vote counts,
  percentages, and winners. Frontend only renders what the API returns, never computes
  results itself.
- **Tie detection:** if two or more candidates share the highest vote count, the
  position is marked `isTie: true` and no `winner` is set — no random tiebreaker.
- **Live results gating:** student results endpoint checks the election's
  `showLiveResults` flag (defaults to `false`) — before the election is COMPLETED,
  students only see results if an admin explicitly enabled live results. Admins can
  always see results via `/admin/results`.
- Pages: `/student/vote`, `/student/vote-success`, `/student/results?id=X`,
  `/admin/results` (with an election picker)

### New endpoints in this phase
```
POST   /api/votes                          Cast a vote (student, from JWT identity)
GET    /api/votes/status/{electionId}      Per-position voted/not-voted for the student

GET    /api/student/elections/{id}/results Gated by COMPLETED status or showLiveResults
GET    /api/admin/elections/{id}/results   Always visible to admins
```

## To test Phase 3
1. As admin: create an election with a start time in the past few minutes and an end
   time in the future (so it's ACTIVE immediately), add a position, add an approved
   candidate (register a student first, then add them as a candidate and approve them).
2. Log in as that student (or a different one) → View Elections → the election shows
   ACTIVE → open it → click **Vote Now** on the position.
3. Select the candidate → **Review My Vote** → **Confirm Vote** → you land on the
   success page.
4. Go back to the election — that position now shows "You voted" and the Vote button is
   gone. Try voting again via a second browser tab / student account to see multi-voter
   turnout build up.
5. Close the election (admin) or enable `showLiveResults` when creating it, then check
   **View Results** as a student, and `/admin/results` as admin — you should see matching
   vote counts, percentages, and a winner (or a TIE badge if you make two candidates tie).

## What's still open from the original 40-section spec
Not yet built: real SMTP email delivery (OTP is returned in the API response instead —
see Phase 5 notes below), search result pagination for very large student lists, and
PDF export (CSV export is fully working for both students and results).

## Phase 5 — DONE (included in this zip)
Security hardening, self-service password management, and the remaining admin tooling:

- **Login rate limiting**: 5 failed attempts locks that email out for 15 minutes
  (in-memory — resets on app restart; fine for a single-instance college deployment,
  swap for Redis if you ever run multiple instances).
- **Forgot/reset password via OTP**: `/forgot-password` → 6-digit code, hashed and
  stored server-side, expires in 5 minutes, 60-second resend cooldown → `/reset-password`
  verifies the code and sets a new password. **No real SMTP is wired up** (that needs
  your actual mail credentials in `application.properties`, which I can't supply for
  you) — so for now the OTP is returned directly in the API response and shown on the
  forgot-password page so the whole flow is testable end-to-end. Wire in
  `JavaMailSender` using the already-scaffolded `spring.mail.*` properties before using
  this for real, and stop returning `devOnlyOtp` once you do.
- **Change password** (logged-in students): `/student/change-password`, requires the
  current password.
- **Candidate photo upload**: real file upload (JPG/PNG/WEBP, 5MB limit) via
  `/admin/candidates` → Add Candidate → Photo field, stored to `./uploads/candidates/`
  and served back at `/uploads/candidates/...`. The old `photoUrl` string field still
  works too if you'd rather paste a URL.
- **Admin candidate & position management UI**: this was previously API-only — now
  `/admin/candidates` lets you pick an election, add positions, add candidates (with
  photo), and approve/reject/disqualify them, all without leaving the browser.
- **CSV export of results**: `/admin/results` → Export CSV, alongside the existing
  student CSV export.

### New endpoints in this phase
```
POST   /api/auth/forgot-password
POST   /api/auth/reset-password
GET    /api/student/profile
PUT    /api/student/profile/change-password

POST   /api/admin/candidates/{id}/photo         (multipart image upload)
GET    /api/admin/elections/{id}/results/export (CSV download)
```

## Full endpoint reference (all phases)
```
# Public
POST   /api/auth/register
POST   /api/auth/login
POST   /api/auth/forgot-password
POST   /api/auth/reset-password

# Student (ROLE_STUDENT)
GET    /api/student/profile
PUT    /api/student/profile/change-password
GET    /api/student/elections/active | /upcoming | /completed
GET    /api/student/elections/{id}
GET    /api/student/elections/{id}/results
GET    /api/student/candidates/election/{electionId}
GET    /api/student/candidates/position/{positionId}
GET    /api/student/candidates/{id}
POST   /api/votes
GET    /api/votes/status/{electionId}
GET    /api/student/notifications
GET    /api/student/notifications/unread-count
PATCH  /api/student/notifications/{id}/read

# Admin (ROLE_ADMIN)
GET/POST/PUT   /api/admin/elections[/{id}]
PATCH          /api/admin/elections/{id}/start | /close | /cancel
GET            /api/admin/elections/{id}/results
GET            /api/admin/elections/{id}/results/export
GET/POST/PUT/DELETE  /api/admin/elections/{electionId}/positions[/{id}]
GET/POST/PUT   /api/admin/candidates[/{id}]
GET            /api/admin/candidates/election/{electionId}
PATCH          /api/admin/candidates/{id}/approve | /reject | /disqualify
POST           /api/admin/candidates/{id}/photo
GET/POST/PUT   /api/admin/students[/{id}]
PATCH          /api/admin/students/{id}/activate | /deactivate
POST           /api/admin/students/import
GET            /api/admin/students/export
GET            /api/admin/dashboard/stats
GET            /api/admin/audit-logs
```

## To test Phase 5
1. **Rate limiting**: try logging in with a wrong password 5 times — the 6th attempt
   (even with the right password) should be rejected with a lockout message.
2. **Password reset**: log out → Forgot password → enter a registered student email →
   you'll see the code directly on the page (dev mode) → it also copies you to
   `/reset-password` → enter the code + new password → log in with the new password.
3. **Change password**: log in as a student → Change Password → old password + new one.
4. **Candidate photo**: `/admin/candidates` → pick an election → add a position → Add
   Candidate → attach an image → it should appear on the student-facing ballot and
   election details page.
5. **CSV results export**: `/admin/results` → pick an election with votes → Export CSV.

This closes out all 5 phases. The system now covers registration, auth (with rate
limiting + password reset), full election/position/candidate lifecycle management,
voting with DB-enforced integrity, results with tie detection, student management with
CSV import/export, notifications, audit logging, and photo uploads — the functional
core of the original spec, built and tested a working slice at a time.

## Phase 4 — DONE (included in this zip)
Student management, real dashboard stats, notifications, and audit logs:
- **Student management** (`/admin/students`): search/filter by name, student ID, email,
  department, course, year; add/edit students (Student ID locked after creation, per
  spec); activate/deactivate accounts.
- **CSV import**: upload a CSV with header `student_id,name,email,department,course,year`.
  Every row is validated — missing fields, duplicate student ID/email, and malformed
  rows are all caught individually and reported back in a summary (total / successful /
  failed / duplicates + per-row error list), not silently skipped or a single all-or-
  nothing failure. Imported students get a default password (`Student@123`) since the
  spec's CSV format has no password column — communicate it to students separately.
- **CSV export**: `/admin/students` → Export CSV button, downloads a real file built
  from live DB data.
- **Admin dashboard stats** are now genuinely real — no hardcoded numbers. Total
  students/candidates/elections, active elections, votes cast, and turnout are all
  live queries, plus department- and year-wise turnout bar charts for whichever
  election is currently active (or most recent, if none are).
- **Notifications**: students automatically get an in-app notification when an election
  they're eligible for opens (on Start) and when results become available (on Close).
  `/student/notifications` lists them with read/unread state; the dashboard shows an
  unread-count badge.
- **Audit logs**: every meaningful admin action (election created/updated/started/
  closed/cancelled, candidate created/approved/rejected/disqualified, student created/
  updated/activated/deactivated, CSV imports) is recorded with which admin did it and
  when. `/admin/audit-logs` supports filtering by action text or entity type.

### New endpoints in this phase
```
GET    /api/admin/students?query=&department=&course=&year=
POST   /api/admin/students
PUT    /api/admin/students/{id}
PATCH  /api/admin/students/{id}/activate
PATCH  /api/admin/students/{id}/deactivate
POST   /api/admin/students/import      (multipart CSV)
GET    /api/admin/students/export      (downloads CSV)

GET    /api/admin/dashboard/stats

GET    /api/student/notifications
GET    /api/student/notifications/unread-count
PATCH  /api/student/notifications/{id}/read

GET    /api/admin/audit-logs?action=&entityType=
```

## To test Phase 4
1. As admin: go to **Manage Students** → try adding a student manually, then try
   **Import CSV** with a file like:
   ```
   student_id,name,email,department,course,year
   S2001,Asha Rao,asha.rao@college.edu,CSE,BTech,2nd Year
   S2002,Vikram Nair,vikram.nair@college.edu,ECE,BTech,3rd Year
   ```
   You should see a summary with successful/failed/duplicate counts.
2. Search/filter the student list; deactivate one, confirm it flips to DISABLED and the
   button changes to Activate.
3. Export CSV and confirm it downloads with all current students.
4. Go to **Dashboard** — confirm the stat cards and charts reflect real numbers (create/
   start an election and vote on it from Phase 3 to see the charts populate).
5. Start or close an election → log in as a student → check the notification badge on
   the dashboard and the full list at **Notifications**.
6. As admin, check **Audit Logs** — every action you just took (student created,
   election started, CSV import, etc.) should be listed with a timestamp.

## Next: Phase 5
OTP/email verification, password reset, security hardening (login rate limiting), and
final UI polish. This closes out the spec's optional/nice-to-have items — the core
system (auth, elections, voting, results, admin tooling) is functionally complete as of
Phase 4.
