# Backlog

This is the executable work queue. Items should be small enough that a coding agent can complete them
in a bounded task, with clear acceptance criteria.

## How to add items

- Items originate from `planning/inbox/` entries and get compiled here during a manual planning session.
- Each item should include:
  - goal
  - scope
  - acceptance criteria (testable)
  - implementation notes (paths, constraints)
  - validation plan

## Active

### P0

- [ ] Backend walking skeleton — health check + DB connectivity
  - **Goal:** Stand up a minimal Java Spring Boot backend that starts
    successfully, connects to a local MySQL database, and exposes a
    health-check endpoint proving the DB connection is alive.
  - **Scope:**
    - New `backend/` Maven-based Spring Boot project (Java 17 or 21).
    - Dependencies: spring-boot-starter-web, a JDBC/MySQL driver, and
      flyway-mysql.
    - One Flyway baseline migration (`V1__init.sql`) proving Flyway runs on
      startup (may be a no-op or placeholder statement).
    - `GET /api/health` runs a trivial query (e.g. `SELECT 1`) against MySQL
      and returns JSON indicating whether the database is connected.
    - Root-level `docker-compose.yml` with a single `db` service
      (`mysql:8`), a named volume for persistence, and env vars matching the
      backend's `application.yml`.
  - **Acceptance criteria:**
    - [ ] Running `docker compose up -d` starts a MySQL container reachable
      on localhost:3306.
    - [ ] Running the backend (`mvn spring-boot:run` from `backend/`) starts
      without errors and applies the Flyway migration on startup.
    - [ ] `GET http://localhost:8080/api/health` returns HTTP 200 with a
      JSON body indicating the database is connected, while MySQL is running.
    - [ ] Stopping the MySQL container and hitting `/api/health` again
      returns a response indicating the database is not reachable, proving
      the check is live rather than hardcoded.
  - **Notes (paths/constraints):** `backend/` (new top-level dir),
    `backend/src/main/resources/db/migration/V1__init.sql`,
    `backend/src/main/resources/application.yml`, `docker-compose.yml`
    (repo root). No authentication, business tables, or habit logic yet —
    infrastructure only.
  - **Validation:** Jessie runs `docker compose up -d` in a terminal, starts
    the backend, and opens `http://localhost:8080/api/health` in a browser
    (or via `curl`) to confirm the database shows as connected. Jessie then
    runs `docker compose stop db` and reloads the same URL, confirming the
    response changes to reflect the database is no longer reachable.

- [ ] Frontend walking skeleton hitting backend health check
  - **Goal:** Stand up a minimal TypeScript + React frontend that calls the
    backend's health-check endpoint and visibly displays whether the
    backend/database are reachable.
  - **Scope:**
    - New `frontend/` Vite + React + TypeScript scaffold.
    - Single page that, on load, fetches `GET http://localhost:8080/api/health`
      and displays the result as plain text (e.g. "Backend: connected" /
      "Backend: unreachable").
    - No routing, styling framework, or state-management library — plain
      `fetch` + `useState`.
    - CORS must be enabled on the backend for `http://localhost:5173`
      (coordinate with the backend health-check item).
  - **Acceptance criteria:**
    - [ ] Running `npm install && npm run dev` inside `frontend/` starts a
      dev server on `localhost:5173` without errors.
    - [ ] With backend and MySQL both running, opening the page shows a
      visible message confirming the backend/database are reachable.
    - [ ] With the backend stopped (frontend still running), reloading the
      page shows a visible "unreachable" message instead of a blank page or
      unhandled error.
  - **Notes (paths/constraints):** `frontend/` (new top-level dir). Depends
    on the backend health-check endpoint and CORS being enabled. No UI
    polish beyond plain HTML/CSS.
  - **Validation:** With backend + MySQL running, Jessie opens
    `http://localhost:5173` in a browser and confirms it shows a "connected"
    message. Jessie stops the backend process and reloads the page,
    confirming it now shows a clear "unreachable" message.

- [ ] Habit CRUD — backend API (add / list / delete)
  - **Goal:** Allow habits (name only, no check-in state yet) to be created,
    listed, and deleted through a backend REST API backed by MySQL.
  - **Scope:**
    - New Flyway migration `V2__create_habits_table.sql` creating a
      `habits` table: `id` (auto-increment PK), `name` (varchar, not null),
      `created_at` (timestamp, default now).
    - `POST /api/habits` (body `{"name": "..."}`) creates a habit.
    - `GET /api/habits` returns all habits as a JSON array.
    - `DELETE /api/habits/{id}` deletes a habit by id.
    - Reject `POST` with an empty/blank name (HTTP 400).
    - No update/edit endpoint, no check-in fields.
  - **Acceptance criteria:**
    - [ ] `POST /api/habits` with a valid name returns HTTP 201 and the
      created habit (with an id) as JSON.
    - [ ] `POST /api/habits` with an empty/missing name returns HTTP 400 and
      does not create a row.
    - [ ] `GET /api/habits` returns all previously created habits, including
      ones from an earlier request (proving persistence, not in-memory
      only).
    - [ ] `DELETE /api/habits/{id}` for an existing id removes it — a
      subsequent `GET /api/habits` no longer includes it.
    - [ ] `DELETE /api/habits/{id}` for a non-existent id returns 404, not a
      500 error.
    - [ ] Restarting the backend process (without touching the database)
      preserves previously created habits.
  - **Notes (paths/constraints):**
    `backend/src/main/resources/db/migration/V2__create_habits_table.sql`,
    new `backend/src/main/java/.../habit/` package (controller, service,
    repository, entity). No `user_id` column — table is single-user by
    design.
  - **Validation:** Using `curl`/Postman (or a browser for GET), Jessie
    sends a `POST` to `/api/habits` with a name and confirms a 201 with the
    new habit's id. Jessie calls `GET /api/habits` and confirms it appears.
    Jessie calls `DELETE` with that id, then `GET` again to confirm it's
    gone. Jessie restarts the backend and calls `GET` again to confirm
    remaining habits survive the restart.

- [ ] Habit CRUD — frontend UI (add / list / delete)
  - **Goal:** Let a user add, view, and delete habits from the browser,
    using the backend CRUD API.
  - **Scope:**
    - A text input + "Add" button to create a habit.
    - A list showing all existing habits (name only) with a "Delete"
      button/icon next to each.
    - On add: calls `POST /api/habits`, then refreshes the list.
    - On delete: calls `DELETE /api/habits/{id}`, then refreshes the list.
    - Empty-state message when there are no habits ("No habits yet — add
      one above.").
    - No check-in UI yet, no styling polish beyond basic readability.
  - **Acceptance criteria:**
    - [ ] Typing a name and clicking "Add" causes the new habit to appear in
      the visible list without a full page reload.
    - [ ] Reloading the browser (F5) still shows the habit (proving it
      reads from the backend/DB, not local state only).
    - [ ] Clicking "Delete" removes a habit from the visible list, and
      reloading confirms it stays deleted.
    - [ ] Attempting to add a habit with a blank name does not create an
      empty-named entry (button disabled or an inline message appears).
    - [ ] With zero habits, the page shows the "No habits yet" message
      instead of a blank area.
  - **Notes (paths/constraints):** `frontend/src/` — a single `App.tsx` or
    small components (`HabitList`, `HabitForm`); no routing library needed.
    Depends on the habit CRUD backend endpoints.
  - **Validation:** Jessie opens `http://localhost:5173`, types a habit name
    (e.g. "Drink water"), clicks Add, and confirms it appears in the list.
    Jessie reloads the browser and confirms it's still listed. Jessie clicks
    Delete, confirms it disappears, reloads again, and confirms it does not
    reappear.

### P1

- [ ] Daily check-in — backend API
  - **Goal:** Allow each habit to be marked done/not-done for "today" via
    the backend API, with the state persisted in MySQL and readable per
    habit.
  - **Scope:**
    - New Flyway migration `V3__create_habit_checkins_table.sql`: a
      `habit_checkins` table — `id` (PK), `habit_id` (FK to `habits.id`),
      `checkin_date` (date, not null), `done` (boolean, not null), with a
      unique constraint on `(habit_id, checkin_date)` so a habit can only
      have one check-in row per calendar day.
    - `PUT /api/habits/{id}/checkins/today` (body `{"done": true|false}`)
      creates or updates today's check-in row for that habit (upsert).
    - `GET /api/habits/{id}/checkins/today` returns today's check-in state
      (defaulting to `{"done": false}` if no row exists yet).
    - "Today" is determined by the server's clock, not a client-supplied
      date — no timezone handling in scope for v1.
    - No endpoints for past dates, history, or streak calculation.
  - **Acceptance criteria:**
    - [ ] `PUT .../checkins/today` with `{"done": true}` for a habit with no
      prior check-in today creates a new row and returns the updated state.
    - [ ] Calling the same endpoint again the same day with
      `{"done": false}` updates the same row (does not create a second row).
    - [ ] `GET .../checkins/today` for a habit with no check-in yet today
      returns `{"done": false}` without error (not a 404).
    - [ ] `PUT` for a non-existent habit id returns 404.
    - [ ] Restarting the backend process preserves today's check-in state.
  - **Notes (paths/constraints):**
    `backend/src/main/resources/db/migration/V3__create_habit_checkins_table.sql`,
    new checkin controller/service/repository under the habit package.
    Depends on the habits table existing. No support for editing check-ins
    on dates other than today in v1.
  - **Validation:** Jessie uses `curl`/Postman to `PUT {"done": true}` to a
    habit's today check-in endpoint, then calls the matching `GET` and
    confirms it reflects `"done": true`. Jessie toggles it to false the same
    way and confirms `GET` reflects the change. Jessie restarts the backend
    and calls `GET` again to confirm the state survived the restart.

- [ ] Daily check-in — frontend UI
  - **Goal:** Let a user mark each habit as done/not-done for today directly
    from the habit list, reflecting what's stored in the backend.
  - **Scope:**
    - Add a checkbox/toggle next to each habit in the existing list,
      reflecting today's check-in state (fetched via
      `GET .../checkins/today` when the list loads).
    - Toggling it calls `PUT .../checkins/today` with the new value.
    - No history view, per-day navigation, or calendar/streak visuals.
  - **Acceptance criteria:**
    - [ ] On page load, each habit's toggle reflects its actual stored
      today-state (checked if `done=true`, unchecked otherwise).
    - [ ] Clicking an unchecked toggle marks it checked and persists —
      reloading shows it still checked.
    - [ ] Clicking a checked toggle marks it unchecked and persists —
      reloading shows it still unchecked.
    - [ ] Adding a brand-new habit shows its toggle as unchecked by default.
  - **Notes (paths/constraints):** `frontend/src/` — extend the habit list
    item with a checkbox bound to the check-in endpoints. Depends on the
    check-in backend API and the habit CRUD frontend.
  - **Validation:** Jessie opens `http://localhost:5173`, clicks the
    checkbox next to a habit to mark it done today, reloads the page, and
    confirms the checkbox is still checked. Jessie unchecks it, reloads, and
    confirms it now shows unchecked.

### P2

- [ ] AI Habit Coach — backend tool-calling integration
  - **Goal:** Let a user manage habits and check-ins by chatting in natural
    language with an AI agent that actually performs the requested action
    via tool calls against the app's own habit/check-in logic.
  - **Scope:**
    - Add `com.anthropic:anthropic-java` as a Maven dependency in
      `backend/pom.xml`.
    - A Spring `@Bean` producing an Anthropic client via
      `AnthropicOkHttpClient.fromEnv()` (reads `ANTHROPIC_API_KEY` from the
      environment — never hardcoded).
    - Define four tools for model `claude-sonnet-5`: `get_habits` (no
      input; returns each habit's id, name, and today's done state),
      `add_habit` (`{name: string}`), `delete_habit`
      (`{habit_id: integer}`), `set_checkin`
      (`{habit_id: integer, done: boolean}`) — each backed by the *existing*
      habit/check-in services, no new business logic.
    - `POST /api/coach/messages` accepts the full conversation history from
      the frontend, runs the standard tool-use loop (send request →
      execute any `tool_use` blocks by calling the matching Java service →
      send `tool_result`(s) back → repeat until `stop_reason` is
      `end_turn`), and returns the final assistant text plus the updated
      content blocks the frontend needs to keep as history.
    - No server-side chat storage — conversation state lives entirely in
      the request/response round-trip.
  - **Acceptance criteria:**
    - [ ] Sending `{"message": "add a habit called Drink Water"}` (with
      empty prior history) to `/api/coach/messages` results in a new habit
      named "Drink Water" existing in `GET /api/habits` afterward.
    - [ ] Sending a message asking to check off an existing habit by name
      results in that habit's `GET .../checkins/today` reflecting
      `"done": true` afterward.
    - [ ] Sending a message asking "what habits do I have" returns assistant
      text that accurately lists the habits currently in the database (not
      a stale or hallucinated list).
    - [ ] If `ANTHROPIC_API_KEY` is not set when the backend starts, the
      coach endpoint fails clearly (e.g. a 500 with a descriptive error) at
      request time rather than silently doing nothing.
  - **Notes (paths/constraints):** `backend/pom.xml`, new
    `backend/src/main/java/.../coach/` package (client config bean, tool
    definitions, controller). Depends on the habit CRUD and check-in backend
    items already existing (tools call their services directly). Requires
    `ANTHROPIC_API_KEY` exported in the shell before running the backend.
  - **Validation:** With `ANTHROPIC_API_KEY` set and the backend running,
    Jessie uses `curl`/Postman to `POST /api/coach/messages` with
    `{"message": "add a habit called Drink Water"}`, then calls
    `GET /api/habits` and confirms "Drink Water" is now present — proving
    the tool call actually executed against the database, not just a
    plausible-sounding reply.

- [ ] AI Habit Coach — frontend chat UI
  - **Goal:** Let a user talk to the Habit Coach from the browser and see
    its actions reflected in the regular habit list/check-in UI.
  - **Scope:**
    - A simple chat panel (scrollable message list + text input + send
      button) added to the existing habit UI.
    - Conversation history kept in React state; each send posts the full
      history to `POST /api/coach/messages` and appends the response.
    - After each coach response, refresh the habit list/check-in state so
      changes the coach made are immediately visible in the regular UI.
    - No markdown rendering, streaming, or chat history persistence needed
      for v1 — plain text messages are enough.
  - **Acceptance criteria:**
    - [ ] Typing "add a habit called Read" into the chat and sending it
      shows an assistant reply, and "Read" appears in the regular habit
      list without a manual page reload.
    - [ ] Typing a message to check off an existing habit results in that
      habit's checkbox becoming checked in the regular list, without a
      manual reload.
    - [ ] Reloading the browser clears the chat panel's message history
      (expected — no persistence in v1) but the habit list/check-ins remain
      correct, since those are backend-persisted.
  - **Notes (paths/constraints):** `frontend/src/` — a new `Coach` or
    `ChatPanel` component alongside the existing habit list. Depends on the
    coach backend endpoint.
  - **Validation:** Jessie opens `http://localhost:5173`, types "add a habit
    called Read" in the chat panel, sends it, and confirms both an
    assistant reply appears and "Read" shows up in the habit list below
    without needing to refresh the page.

### P3

- [ ] Coach accuracy eval — CAT Cafe dataset + experiment
  - **Goal:** Measure the Habit Coach's tool-selection accuracy against a
    labeled dataset, rather than relying on manual spot-checks, and confirm
    it meets the 90% target set in `plan.md`.
  - **Scope:**
    - Set up Artium's `eval-workshop` tooling
      (`https://github.com/thisisartium/eval-workshop`) locally, separate
      from this project's own stack: clone it, `uv sync`, configure its
      `.env` with an Anthropic API key, and `docker compose up -d` to start
      the `cat-cafe` service.
    - Build a small labeled dataset (aiming for at least ~20-30 examples)
      of representative user messages mapped to the expected tool call and
      arguments — e.g. "add a habit called Drink Water" →
      `add_habit({"name": "Drink Water"})`; "mark meditation done" →
      `set_checkin({"habit_id": <id>, "done": true})`; ambiguous or
      off-topic messages mapped to "no tool call" where appropriate.
    - Register the dataset with the eval-workshop tooling and run an
      experiment against the Habit Coach's actual backend endpoint,
      following the same pattern as the workshop's `cat-experiments run`
      flow.
    - Review results in CAT Cafe (`http://localhost:8000/datasets`),
      including any confusion patterns (e.g. `add_habit` vs `set_checkin`
      mix-ups), and iterate on the coach's system prompt / tool
      descriptions until overall accuracy reaches at least 90%.
  - **Acceptance criteria:**
    - [ ] A labeled eval dataset of representative coach interactions is
      registered and viewable at `http://localhost:8000/datasets`.
    - [ ] At least one experiment has been run against the Habit Coach
      backend and its results (accuracy, and ideally a confusion breakdown)
      are viewable in CAT Cafe.
    - [ ] The most recent experiment run reports overall tool-selection
      accuracy of **at least 90%**.
    - [ ] Any prompt/tool-description changes made to reach that threshold
      are reflected in the actual coach code (P2 backend item), not just in
      the eval tooling.
  - **Notes (paths/constraints):** This item touches the separate
    `eval-workshop` checkout/environment, not files inside `pm-practice-lab`
    — except for the coach's own system prompt/tool descriptions in
    `backend/src/main/java/.../coach/`, which may be edited as a result of
    what the evals reveal. CAT Cafe and its Docker/Python tooling are
    dev-time only and are never bundled into this app's own
    `docker-compose.yml` or deployment.
  - **Validation:** Jessie opens `http://localhost:8000/datasets` in a
    browser, opens the registered dataset's experiment results, and
    confirms the reported overall accuracy is at least 90%.

## Icebox

- History view / calendar of past check-ins per habit.
- Streaks, badges, or other gamification/scoring.
- Push notifications or reminders.
- Multi-user support, accounts, authentication/login.
- Syncing across devices or cloud backup.
- Data visualization / charts / trend graphs.
- Editing/renaming an existing habit (only add/list/delete are in scope for v1).
- Quantity-based habit completion (only binary done/not-done is in scope).
- Persisting Habit Coach chat history across browser sessions.
- Habit Coach available outside the app (SMS, email, Slack, etc.).
