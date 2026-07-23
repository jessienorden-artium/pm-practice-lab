# Planning Changelog

Append-only log of changes applied to `planning/compiled/`.

## Entry template

### YYYY-MM-DD — <short summary>

- **Source inbox files:** `planning/inbox/...`
- **Change summary:**
  - (What changed in plan/backlog)
- **Rationale:**
- **Assumptions / open questions:**
- **Notes on impact (optional):**
  - (Metrics, migrations, compatibility, sequencing)

### 2026-07-22 — Compile habit tracker inbox entry into first plan/backlog, including an AI Habit Coach agent and accuracy evals

- **Source inbox files:** `planning/inbox/2026-07-21-1638_claude-discussion_habit-tracker-mock-project.md`
- **Change summary:**
  - Filled in `plan.md`'s overview, north star outcomes, non-goals, current
    phase, five milestones (walking skeleton, habit CRUD, daily check-in, AI
    Habit Coach agent, coach accuracy evaluation), dependencies/constraints
    (TypeScript+React frontend, Java Spring Boot backend, MySQL, Flyway
    migrations, docker-compose for local MySQL only, the Anthropic Java SDK
    + `claude-sonnet-5` for the coach, CAT Cafe for coach evals), and risks
    with mitigations.
  - Added four P0 backlog items (backend walking skeleton, frontend
    walking skeleton, habit CRUD backend, habit CRUD frontend), two P1 items
    (daily check-in backend, daily check-in frontend), two P2 items (Habit
    Coach backend tool-calling integration, Habit Coach frontend chat UI),
    and one P3 item (coach accuracy eval using CAT Cafe), each with
    goal/scope/acceptance criteria/notes/validation.
  - Added an Icebox list covering history, streaks, notifications,
    multi-user/auth, sync, charts, chat-history persistence, and
    coach-outside-the-app channels — matching the inbox entry's
    non-committal/excluded ideas plus new exclusions from the coach scope.
- **Rationale:** The inbox entry's open questions (platform, fidelity,
  definition of "done") were answered directly by Jessie: a real, working
  mobile-friendly TypeScript+Java+MySQL web app, binary check-in. Jessie
  then clarified the actual learning goal was to implement an AI agent
  themselves, not just use a coding assistant to build a CRUD app — so the
  app now includes a chat-based "Habit Coach" using tool-calling against the
  app's own habit/check-in logic (Claude Sonnet 5, chosen for cost given
  this is a personal project), plus an explicit accuracy target (90%
  tool-selection accuracy) measured via CAT Cafe, Artium's eval-workshop
  tooling, rather than relying on manual spot-checks. This compilation
  captures all of that in canonical planning and sequences work as a
  walking-skeleton-first build so a coding agent can execute small,
  independently verifiable slices.
- **Assumptions / open questions:**
  - Assumes Java 17/21 + Maven + Spring Boot and Vite + React + TypeScript
    as the specific toolchain within the stack Jessie specified; this
    toolchain choice was not recorded in a separate inbox entry — flagged
    here for confirmation during this planning session.
  - Assumes docker-compose is acceptable for local MySQL (requires Docker
    Desktop installed) — not yet confirmed with Jessie.
  - The coach's specific tool surface (`get_habits`, `add_habit`,
    `delete_habit`, `set_checkin`) is a design choice made during this
    compilation, not literally specified in the inbox entry — flagged for
    Jessie to review.
  - The eval dataset size (~20-30 examples) for the P3 CAT Cafe item is a
    starting-point estimate, not a hard requirement — may need to grow if
    90% accuracy isn't reached or isn't statistically meaningful at that
    size.
  - Open: whether a future inbox entry is needed before P1/P2/P3 work
    starts, or whether this compilation is sufficient to let a coding agent
    proceed through all of them in sequence.
- **Notes on impact (optional):**
  - This is the first-ever compiled plan/backlog for this repo; no prior
    content is being overwritten, only placeholders replaced.
  - Introduces new top-level directories not yet reflected in
    `docs/repo_structure.md` (`backend/`, `frontend/`, root
    `docker-compose.yml`) — that doc should be updated in the same PR that
    adds these directories.
  - Introduces a new external paid dependency (Anthropic API usage) and a
    secret (`ANTHROPIC_API_KEY`) not present in earlier milestones.
  - Introduces a dependency on a separate external tool/repo (CAT Cafe /
    `eval-workshop`) purely for dev-time evaluation of the coach — not part
    of this app's own runtime or deployment.

### 2026-07-23 — Execute P0-1: backend walking skeleton (health check + DB connectivity)

- **Source inbox files:** `planning/inbox/2026-07-21-1638_claude-discussion_habit-tracker-mock-project.md`
- **Change summary:**
  - Marked backlog item P0-1 ("Backend walking skeleton — health check + DB
    connectivity") and its four acceptance criteria complete in
    `backlog.md`.
- **Rationale:** Implemented and validated per the backlog item's own
  Validation steps: `backend/` (Spring Boot, Java 21, Maven), a Flyway
  baseline migration, `GET /api/health` backed by a live `SELECT 1` against
  MySQL, and a root `docker-compose.yml` running MySQL. Confirmed the
  health check reports `connected` with MySQL running and `disconnected`
  (HTTP 200, live check) with MySQL stopped, then confirmed it recovers
  once MySQL is restarted.
- **Assumptions / open questions:** None beyond what's already flagged
  above (toolchain versions, docker-compose requiring Docker Desktop).
- **Notes on impact (optional):**
  - Installed Java 21 (Homebrew `openjdk@21`) and Maven locally, and fixed
    `~/.zshrc` so `java`/`mvn` resolve to Java 21 rather than an unrelated
    newer JDK Maven had pulled in as a dependency.
  - Discovered port 8080 was occupied by leftover containers from an
    earlier, unrelated `eval-workshop` (CAT Cafe) exercise; stopped those
    containers (not removed — restart with `docker start` when picking up
    the P3 coach-eval item) to free the port for this backend.

### 2026-07-23 — Execute P0-2: frontend walking skeleton

- **Source inbox files:** `planning/inbox/2026-07-21-1638_claude-discussion_habit-tracker-mock-project.md`
- **Change summary:**
  - Marked backlog item P0-2 ("Frontend walking skeleton hitting backend
    health check") and its three acceptance criteria complete in
    `backlog.md`.
- **Rationale:** Scaffolded `frontend/` (Vite + React + TypeScript),
  replaced the template `App.tsx` with a minimal page that fetches
  `GET /api/health` on load and displays a plain-text connected/unreachable
  message, and added a `CorsConfig` bean to the backend allowing
  `http://localhost:5173`. Verified in an actual browser (not just curl):
  the page shows "Backend: connected" with the backend running, and
  "Backend: unreachable" (no blank page or crash) with the backend stopped,
  recovering once the backend restarts.
- **Assumptions / open questions:** None new.
- **Notes on impact (optional):**
  - Added `backend/src/main/java/com/habittracker/config/CorsConfig.java` —
    a general `/api/**` CORS mapping for `http://localhost:5173`, intended
    to cover future endpoints (habit CRUD, check-ins, coach) too, not just
    the health check.

### 2026-07-23 — Execute P0-3: habit CRUD backend API

- **Source inbox files:** `planning/inbox/2026-07-21-1638_claude-discussion_habit-tracker-mock-project.md`
- **Change summary:**
  - Marked backlog item P0-3 ("Habit CRUD — backend API") and its six
    acceptance criteria complete in `backlog.md`.
- **Rationale:** Implemented `POST /api/habits`, `GET /api/habits`, and
  `DELETE /api/habits/{id}` (Spring Data JPA + a new `V2__create_habits_table.sql`
  Flyway migration), built test-first: each acceptance criterion got a
  failing integration test (via Testcontainers + a real MySQL) confirmed
  red before the minimal implementation was added to turn it green. Also
  validated manually via `curl` against the running dev backend/database,
  including a backend restart to confirm persistence.
- **Assumptions / open questions:** None new.
- **Notes on impact (optional):**
  - Overrode the Testcontainers version pinned by Spring Boot 3.3.5's BOM
    (1.19.8 → 1.21.4) in `backend/pom.xml` — the older version was
    incompatible with this machine's Docker Desktop and caused every
    Testcontainers-based test to fail with a `BadRequestException` before
    any container could start.
  - Backend CRUD logic was built test-first (red → green per acceptance
    criterion) rather than writing implementation and tests together, per
    Jessie's request.

### 2026-07-23 — Execute P0-4: habit CRUD frontend UI

- **Source inbox files:** `planning/inbox/2026-07-21-1638_claude-discussion_habit-tracker-mock-project.md`
- **Change summary:**
  - Marked backlog item P0-4 ("Habit CRUD — frontend UI") and its five
    acceptance criteria complete in `backlog.md`.
- **Rationale:** Added a `Habits` component (add/list/delete, empty-state
  message, blank-name rejection) to the frontend, built test-first with
  Vitest + React Testing Library — each acceptance criterion got a failing
  component test confirmed red before the minimal implementation was added.
  Manually validated in an actual Chrome browser against the real running
  backend/database: add, reload-persists, delete, reload-stays-deleted, and
  the empty state, all confirmed visually.
- **Assumptions / open questions:** None new.
- **Notes on impact (optional):**
  - Set up frontend testing infrastructure (Vitest, React Testing Library,
    jsdom) — none existed before. Pinned `vitest` to the latest release
    (^4.1.10) rather than an older version, to avoid a vulnerable transitive
    `esbuild` dependency.
  - During manual browser validation, a `DELETE` request returned a
    spurious `503` on a backend process that had been running ~28 hours
    with long idle/sleep gaps (visible in logs as HikariCP "thread
    starvation or clock leap" warnings); the delete had actually succeeded
    server-side despite the error response. Restarting both the backend and
    frontend processes cleanly resolved it — treated as a transient
    environment issue, not an application bug, since it didn't reproduce
    after a clean restart.
