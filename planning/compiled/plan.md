# Project Plan

## Overview

A single-user habit tracker web app. Users create habits and, each day, mark
each one done or not done. A conversational **AI Habit Coach** lets the user
manage habits and check-ins by talking to it in natural language instead of
only using the list UI directly. "Done" for the app is a working,
mobile-friendly web app where a user can add habits, see them listed, check
them off for the current day, and accomplish the same actions by chatting
with the coach — with everything persisted in a database, and with the
coach's accuracy measured and proven via evals rather than assumed.

## North star outcomes

- A user can create, view, and delete habits.
- A user can mark any habit done or not-done for the current day, and that
  state is saved and reflected correctly when they come back later.
- A user can accomplish the above (adding a habit, checking in, asking how
  they're doing) by chatting with the Habit Coach, which actually performs
  the action rather than just describing it.
- The Habit Coach's success is measured, not assumed: its tool-selection
  accuracy is evaluated against a labeled dataset using CAT Cafe (Artium's
  eval-workshop tooling) and reaches **at least 90%** before the coach is
  considered done.
- The app is usable from a phone via a mobile web browser.

## Non-goals

- No user accounts, login, or authentication.
- No multi-user support, sharing, or household/shared habit tracking.
- No syncing across devices or cloud backup.
- No push notifications or reminders.
- No streaks, badges, scoring, or other gamification.
- No data visualization, charts, or trend graphs.
- No quantity-based habit tracking — check-in is binary (done/not done) only.
- No native mobile app or React Native build — mobile-friendly web app only.
- The Habit Coach is in-app chat only — no external channels (SMS, email,
  Slack, etc.).
- Chat conversation history is not persisted across sessions for v1 — it
  lives only in the browser tab and is lost on refresh.
- Eval tooling (CAT Cafe / eval-workshop) is a development-time evaluation
  aid only — it is not bundled into the running app or deployed anywhere.

## Current phase

- Phase: Build
- Definition of done for this phase: The walking skeleton (Milestone 1) and
  habit CRUD (Milestone 2) are both implemented and pass their Validation
  steps in `backlog.md`. Daily check-in (Milestone 3), the AI Habit Coach
  (Milestone 4), and its accuracy evaluation (Milestone 5) are not required
  to be complete for this phase to be considered done, but are next up in
  order.

## Milestones

1. Walking skeleton
   - Description: A minimal Java Spring Boot backend connects to a local
     MySQL database (schema managed by Flyway) and exposes a health-check
     endpoint; a minimal TypeScript+React frontend calls that endpoint and
     visibly shows whether the backend/database are reachable.
   - Exit criteria: Backlog items P0-1 and P0-2 are both complete and pass
     their Validation steps — opening the frontend in a browser shows a live
     "connected" status backed by a real database round-trip, and the status
     changes correctly when the backend is stopped.

2. Habit CRUD (add / list / delete)
   - Description: A user can add a habit by name, see all their habits
     listed, and delete a habit — end to end through the browser, persisted
     in MySQL. No check-in behavior yet.
   - Exit criteria: Backlog items P0-3 and P0-4 are both complete and pass
     their Validation steps — a habit added via the browser survives a page
     reload and a backend restart, and a deleted habit stays deleted after
     both.

3. Daily binary check-in
   - Description: A user can mark any habit as done or not-done for the
     current calendar day, with the state persisted and reflected correctly
     on reload. No history, streaks, or past-date editing.
   - Exit criteria: Backlog items P1-1 and P1-2 are both complete and pass
     their Validation steps — toggling a habit's today-checkbox in the
     browser persists across a page reload and a backend restart.

4. AI Habit Coach agent
   - Description: A chat panel lets the user talk to an AI agent (Claude
     Sonnet 5, via the Anthropic API called directly from the Java backend)
     that can list habits, add a habit, delete a habit, and set today's
     check-in — by actually calling the app's own habit/check-in logic via
     tool use, not just describing what it would do.
   - Exit criteria: Backlog items P2-1 and P2-2 are both complete and pass
     their Validation steps — asking the coach to add a habit makes it
     appear in the regular habit list (proving the tool call really executed
     against the database), and asking it to check off a habit is reflected
     in the regular UI.

5. Coach accuracy evaluation
   - Description: Build a small labeled eval dataset of representative user
     messages mapped to the tool call (and arguments) the coach should make,
     and run it through CAT Cafe (`https://github.com/thisisartium/eval-workshop`)
     to measure tool-selection accuracy, iterating on the coach's system
     prompt/tool descriptions until the target is met.
   - Exit criteria: Backlog item P3-1 is complete and passes its Validation
     step — an experiment run in CAT Cafe against the eval dataset reports
     **overall tool-selection accuracy of at least 90%**, viewable at
     `http://localhost:8000/datasets`.

## Dependencies and constraints

- Stack: TypeScript + React frontend, Java (Spring Boot) backend, MySQL for
  storage.
- Platform: mobile-friendly responsive web app, not a native or React Native
  mobile app.
- Local MySQL runs via a root-level `docker-compose.yml` (single `db`
  service, no app containers) so dependencies can be started with one command
  (`docker compose up -d`).
- Schema migrations use Flyway, embedded directly in the Spring Boot backend
  (versioned SQL files under `backend/src/main/resources/db/migration`,
  applied automatically on backend startup). All schema changes must go
  through a new Flyway migration file — no manual/ad-hoc schema edits.
- Backend build tool: Maven. Frontend build tool: Vite.
- AI Habit Coach: uses the official `com.anthropic:anthropic-java` SDK
  (Maven dependency), model `claude-sonnet-5`, called directly from the Java
  backend (no separate agent-hosting service). The Anthropic API key is
  supplied via the `ANTHROPIC_API_KEY` environment variable — never
  hardcoded or committed to the repo.
- Coach evaluation uses **CAT Cafe**, from Artium's separate
  `eval-workshop` repo (`https://github.com/thisisartium/eval-workshop`) —
  its own Docker Compose stack and Python/`uv` environment, entirely
  independent of this app's `docker-compose.yml`/backend/frontend. It
  provides a dashboard (`localhost:8000` — `/traces`, `/datasets`) and a
  `cat-experiments` CLI for running a labeled dataset against the agent and
  reporting accuracy, precision/recall, and confusion matrices. Used purely
  as a dev-time evaluation tool for the coach, not integrated into the app.
- No authentication, session, or authorization infrastructure — single-user,
  localhost-only for v1.
- No deployment/hosting target for v1 — runs on localhost only.
- Every backlog item's Validation step must describe an observable,
  plain-language check (browser behavior, curl/API responses) rather than
  relying on "automated tests pass" alone.

## Risks

- Automated tests alone may not surface real usability problems — mitigation:
  pair every acceptance criterion with an observable manual check (browser or
  API-level) in `backlog.md`.
- Scope creep into P1/P2/P3/Icebox features before earlier milestones are
  solid — mitigation: strict P0-before-P1-before-P2-before-P3 backlog
  ordering; coding agents execute only from `backlog.md` and must not expand
  scope.
- Local toolchain (Docker, Java, Maven, Node) may not be fully installed
  before work starts — mitigation: confirmed Node/npm are installed; Java and
  Maven installation is tracked as a setup step before P0-1 execution.
- Flyway migrations could drift from the real database if the schema is
  edited outside a migration file — mitigation: every schema change (habits,
  habit_checkins) must be a new, additive migration file.
- The AI Habit Coach introduces a paid external dependency (Anthropic API
  usage) and a secret (API key) that didn't exist in earlier milestones —
  mitigation: use the lower-cost `claude-sonnet-5` model, keep the key in an
  environment variable only, and document the cost/key-handling explicitly
  so Jessie isn't surprised by either.
- The coach could "sound right" while calling the wrong tool or wrong
  arguments (e.g. checking in the wrong habit) — mitigation: don't rely on
  manual spot-checking alone; measure tool-selection accuracy against a
  labeled dataset via CAT Cafe and require ≥90% before considering the
  coach done.
- CAT Cafe is a separate toolchain (Docker + Python/`uv`) from the rest of
  this project's stack — mitigation: treat it strictly as a dev-time
  evaluation aid, run only when evaluating the coach, and never bundle it
  into the app's own `docker-compose.yml` or deployment.
