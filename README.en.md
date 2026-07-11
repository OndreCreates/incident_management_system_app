# Incident Management System

*[Česká verze](README.md)*

Backend + admin panel for incident management: an explicit state machine, configurable
SLA tracking, escalation, dashboard analytics, and an append-only audit trail. Third
project in the portfolio -- after `identity_server_app` (auth) and
`notification_center_app` (async delivery) -- demonstrating **stateful business logic
and workflow design**.

It's not a PagerDuty clone, and feature parity was never the goal. The goal was to design
and defend a state machine backed by real business rules (SLA, escalation, audit trail)
under interview-style questioning -- not to build as many features as possible.

## Screenshots

| Admin panel -- landing | Login (`identity_server_app`) | API (Swagger UI) |
| --- | --- | --- |
| ![Admin panel landing](docs/screenshots/landing.png) | ![identity_server_app login page](docs/screenshots/identity-login.png) | ![Swagger UI](docs/screenshots/swagger.png) |

The screens behind login (incident list, detail, dashboard) require an OAuth2/PKCE login
through `identity_server_app` with mandatory MFA, so they can't be captured
automatically without the second factor for the seed account. See step 2 in
[Verification steps](#verification-steps-e2e) below for how to walk through the full flow
manually -- anyone with access to the second factor can add screenshots to
`docs/screenshots/` the same way.

## What it does

- **Incidents**: CRUD, an explicit 6-state state machine (`Created → Assigned →
  Investigating → Mitigated → Resolved → Closed`, with a reopen branch back to
  `Investigating`), an append-only timeline/audit trail per incident.
- **Filtering and search**: by status, severity, assigned user/team, case-insensitive
  full-text search over title/description, pagination.
- **Configurable SLA**: policy (SLA window + near-breach threshold) adjustable per
  severity at `/sla-policies`, no redeploy needed. A scheduled job watches for
  breach/near-breach and flips the flags on the incident.
- **Escalation** (near-breach and breach): email via `notification_center_app` and a
  live in-app toast over WebSocket (STOMP) in the admin panel -- both channels
  best-effort, the app never goes down because the notification service is unreachable.
- **Teams**: CRUD, member management, routing an incident to a team as an independent
  audit fact alongside individual assignment.
- **Comments**: add, edit, and delete (soft delete -- the timeline entry stays, only the
  content is hidden) -- author-only.
- **Bulk actions**: bulk status transition and bulk assignment over selected incidents at
  once, with a per-item result (a batch mixing valid and invalid operations still applies
  the valid ones, not all-or-nothing).
- **CSV export** of incidents respecting the currently applied filters.
- **Postmortem**: create/update form, only for incidents in a terminal status
  (Resolved/Closed).
- **Dashboard**: KPIs (active/critical/breached count), average resolution time, SLA
  compliance %, a chart of incidents created over the last 14 days.
- **Auth**: OAuth2 Authorization Code + PKCE via `identity_server_app`, JWT validation
  against JWKS, MFA mandatory for every user.
- **CI/CD**: GitHub Actions on every push/PR (backend tests against a real MySQL,
  frontend type-check + build), Docker Compose deployment.

## Architecture

```
                        ┌─────────────────────┐
  browser ─────────────▶│  admin-panel (:3001) │
                        │  Next.js, OAuth2/PKCE│
                        └──────────┬───────────┘
                                   │ Bearer token
                                   ▼
                        ┌─────────────────────┐        ┌──────────────────────┐
                        │   app (:8080)         │───────▶│ identity_server_app   │
                        │  Spring Boot 3        │  JWKS  │  (:9000, its own      │
                        │  OAuth2 Resource Srv  │        │  docker-compose stack)│
                        └──────────┬────────────┘        └──────────────────────┘
                                   │            │
                                   ▼            └────────▶┌──────────────────────┐
                        ┌─────────────────────┐  X-API-Key│ notification_center   │
                        │   mysql (:3308)       │           │  _app (:8081,          │
                        └─────────────────────┘           │  its own stack)        │
                                                            └──────────────────────┘
```

- **State machine**: a hand-written `Map<Status, Set<Status>>` in
  `IncidentTransitionService`, not a library -- 6 states, essentially linear with one
  reopen branch, doesn't justify Spring State Machine.
- **Auth**: this service neither issues nor manages credentials. JWTs are issued by
  `identity_server_app` and validated here against its JWKS endpoint. The `sub` claim
  carries the user's email (`identity_server_app` puts no numeric ID in the token) --
  hence the external user ID is `VARCHAR` throughout this service, not `BIGINT`.
- **Audit trail**: `incident_timeline` is append-only. No `UPDATE`/`DELETE` against that
  table, ever.
- **Escalation**: a scheduled job calls `notification_center_app`'s REST API (X-API-Key),
  never implements its own email delivery. Best-effort -- an unreachable notification
  service doesn't crash the escalation job, it just gets logged.
- **Admin panel**: server-first Next.js (Server Components + Server Actions) -- the
  access token lives only in a server-side httpOnly cookie, it never reaches the browser.

## Quick start

### Docker Compose (full stack)

Requires a running `identity_server_app` (JWKS + login flow). `notification_center_app`
is only needed for escalation -- without it the app still works, escalation emails just
don't get delivered (logged as a warning).

```bash
# 1. identity_server_app (in its own directory)
cd ../Identity_server_app && docker compose up -d

# 2. this stack (MySQL + backend + admin panel)
cd ../Incident_management_system_app && docker compose up -d --build
```

Once up:

- Admin panel: http://localhost:3001
- API: http://localhost:8080/api/v1
- Swagger UI: http://localhost:8080/swagger-ui.html
- Identity server login: http://localhost:9000/login (seed account
  `admin@identity-server.dev` / `admin123`)

### Escalation (optional, for local verification)

`notification_center_app` also defaults to `:8080`, same as this backend -- when running
both locally outside Docker, run one of them on a different port. Create a client and API
key through its admin API:

```bash
curl -X POST http://localhost:8081/api/v1/admin/clients \
  -H "X-Admin-Key: dev-admin-key-change-me" \
  -H "Content-Type: application/json" \
  -d '{"name":"incident-management-app"}'
```

Copy the returned `apiKey` into `NOTIFICATION_API_KEY` (see `application.yml` /
docker-compose env).

For live in-app WebSocket notifications in the admin panel (a toast on near-breach/breach
escalation), also set `NOTIFICATION_WS_URL` (the browser-facing address of
`notification_center_app`, e.g. `ws://localhost:8081/ws`) and `NOTIFICATION_WS_CLIENT_ID`
(the id of the client created above -- must match whichever client's `apiKey` is in
`NOTIFICATION_API_KEY`). Without them the feature just silently stays off, same "optional
infrastructure" posture as escalation email. Note: these are build args (see
`admin-panel/Dockerfile`), so a change needs `docker compose up -d --build admin-panel`,
not just a container restart.

### Without Docker (local development)

```bash
docker compose up -d mysql          # database only
mvn spring-boot:run                 # backend on :8080

cd admin-panel
cp .env.example .env.local          # and fill in the values
npm install
npm run dev -- -p 3001              # admin panel on :3001
```

### Tests

```bash
mvn test                            # 72 tests: unit (state machine matrix) + integration (against real MySQL)
cd admin-panel && npm run build     # type-check + build of every route
```

CI (`.github/workflows/ci.yml`) runs both on every push/PR to `main` -- backend against a
MySQL service container, admin panel type-check + build.

## Verification steps (E2E)

1. `docker compose up -d --build` in this directory, with `identity_server_app` running
   alongside it -- the whole stack comes up clean (`docker ps` shows three healthy
   containers).
2. Log in through the admin panel (http://localhost:3001) via `identity_server_app`'s
   login flow (the first login requires setting up TOTP -- MFA is mandatory for every
   user, not optional).
3. Create an incident, walk it through the full happy path: Created → Assigned →
   Investigating → Mitigated → Resolved → Closed.
4. Try an invalid transition (e.g. Created → Closed directly) via `/swagger-ui.html` or
   curl -- verify a 409 with body `{"error":"INVALID_TRANSITION","allowed":[...]}`.
5. Create an incident with severity `CRITICAL`, backdate its `sla_deadline` in the
   database (`UPDATE incident SET sla_deadline = NOW() - INTERVAL 1 HOUR WHERE id = ?`),
   wait up to 60s for the scheduled job, verify `sla_breached` flips to `true` and shows
   up on the dashboard.
6. Add a comment, verify it shows up in the timeline feed on the incident's detail page.
7. Create a team at `/teams` in the admin panel, route an incident to it from the
   incident's detail page, verify a `TEAM_ASSIGNMENT` timeline entry.
8. With `notification_center_app` running and `NOTIFICATION_API_KEY` set: backdate an
   incident's `near_breach_at`, wait for the escalation job, verify the email arrives in
   Mailhog (http://localhost:8025).
9. Move an incident to `Resolved`, fill out the postmortem form on its detail page,
   verify it saves and can be edited. Try creating a postmortem for an incident that
   isn't terminal yet -- verify a 409 `POSTMORTEM_NOT_ALLOWED`.
10. At `/sla-policies` adjust the SLA policy for `LOW` (e.g. to 30 minutes), verify
    already-open incidents at that severity keep their original deadline, but a newly
    created incident gets the new, shorter one.
11. At `/dashboard` verify average resolution time, SLA compliance %, and a chart of
    incidents created over the last 14 days all render.
12. At `/incidents` search by a keyword from a title/description (the "Hledat" field) --
    verify the list filters accordingly.
13. Edit your own comment (inline edit), then delete it -- verify the timeline entry
    stays, just with the text `[komentář smazán]`.
14. At `/incidents` check two incidents, run a bulk status transition to a combination
    where one of them doesn't allow the transition -- verify the valid one gets applied
    and the other reports its own failure in the same response.
15. At `/incidents` click "Export CSV" (optionally with a filter set) -- verify
    `incidents.csv` downloads and matches the current filter.
16. With `NOTIFICATION_WS_URL`/`NOTIFICATION_WS_CLIENT_ID` set: trigger a near-breach or
    breach escalation (see step 5/8) and verify a toast notification appears in the admin
    panel without a page refresh.
17. `curl -i http://localhost:8080/api/v1/incidents` without an `Authorization` header --
    verify 401.
18. `mvn test` -- all tests green.

## Notable design decisions

- **Assignment is its own audit fact.** `assigned_user_id` changes go through
  `IncidentAssignmentService`, not inside `IncidentTransitionService` -- assignment and
  status transition are independent concepts (the state machine confirms this too:
  reassignment is modeled as a transition back to `ASSIGNED`, independent of who was
  assigned before), so they get their own `EventType.ASSIGNMENT` timeline entry. Same
  pattern for teams (`IncidentTeamAssignmentService`, `EventType.TEAM_ASSIGNMENT`) --
  routing to a team and assigning an individual are independent.
- **`open-in-view: false` + `LEFT JOIN FETCH`.** The timeline query explicitly fetches
  the associated `IncidentComment` so `TimelineEntryResponse` can show the comment's
  content without a `LazyInitializationException` outside the request-scoped
  transaction. Same pattern for `Team.members`.
- **User ID as email, not `BIGINT`.** Discovered only while wiring up
  `identity_server_app` (Phase 1D) -- the JWT `sub` claim carries the email, not
  `AppUser`'s numeric id. See `V2__user_id_as_email.sql`.
- **`near_breach_at` is its own column, not derived at runtime.** Computed once at
  creation time (80% of the SLA window), same as `sla_deadline` -- a consistent,
  index-friendly query instead of a per-severity `CASE` expression in SQL.
- **Escalation has two independent `*_notified` flags**, not one shared with
  `SlaBreachJob`. Decouples the escalation job from the exact timing of when
  `SlaBreachJob` flips `sla_breached` -- and leaves the Phase 1E job untouched.
- **`NotificationClient` is an interface**, not a concrete class. Mockito's inline mock
  maker fails on JDK 25 (observed on this machine) -- tests swap in a fake implementation
  instead of mocking, which is also just a cleaner approach independent of that JDK
  detail.
- **SLA policy is read at creation time, never recomputed.** `sla_deadline` and
  `near_breach_at` are computed once from `SlaPolicy` when the incident is created and
  then live independently of that table -- changing the policy has no retroactive effect
  on already-open incidents. Same principle as the earlier hardcoded values, just the
  source of truth moved from code to the database.
- **`resolved_at` clears on reopen.** `IncidentTransitionService` sets it on transition to
  `RESOLVED` and clears it on transition back to `INVESTIGATING` -- average resolution
  time always reflects the latest resolution, not a first attempt that ended up reopened.
- **Search is `LIKE`, not MySQL `FULLTEXT MATCH...AGAINST`.** At the data volume this
  portfolio project will ever hold, relevance ranking would add nothing and would cost
  composability with the other `Specification` filters (a native `MATCH` would need its
  own escape hatch outside the Criteria API).
- **`BulkOperationService` is deliberately a separate bean**, not a method inside
  `IncidentService`. Bulk endpoints are per-item, not all-or-nothing -- one invalid item
  in a batch must not roll back the valid ones next to it. Calling `IncidentService`'s
  `@Transactional` methods from a DIFFERENT bean goes through the real Spring proxy and
  gets its own transaction per item; calling from within the same class (self-invocation)
  would bypass the proxy and the whole batch would share one transaction.
- **In-app notifications are sent straight from the browser to `notification_center_app`**,
  not proxied through this backend. The STOMP topic
  (`/topic/notifications/{clientId}`) is scoped to `notification_center_app`'s client id,
  not to a specific user -- verified live (see `NotificationToasts`), not guessed from
  source.
- **`ApiException` is a shared abstract base** for domain exceptions that map to
  `{error, message}` JSON (`IncidentNotFoundException`, `CommentAuthorMismatchException`,
  etc.) -- `GlobalExceptionHandler` has one `@ExceptionHandler` instead of one per type.
  `InvalidTransitionException` stays separate since its body has extra
  `from`/`attempted`/`allowed` fields.

## Known limitations (deliberate, not overlooked)

- **Hard runtime dependency on `identity_server_app`.** Without its JWKS endpoint the app
  doesn't come up functional (mutating endpoints return 401). Acceptable for a portfolio
  demo; a real multi-team environment would want token caching / circuit breaking.
- **Escalation is best-effort, not guaranteed delivery.** `notification_center_app`
  being unreachable → the escalation job logs a warning and moves on, the incident stays
  unnotified until the next poll. Acceptable for a portfolio demo (see
  `NotificationClient`).
- **No automated E2E test with a genuinely issued token.** MFA is mandatory for every
  login to `identity_server_app`, so the full authorization_code+PKCE flow can't be
  scripted without a human TOTP step -- covered by the manual verification above instead;
  the backend's integration tests use a mocked JWT
  (`SecurityMockMvcRequestPostProcessors`).

## Roadmap -- what's next

Phases 1, 2, and 3, plus four "quick-win" improvements (comment edit/delete, bulk
actions, CSV export, in-app WebSocket notifications) are done (see `ROADMAP.md`, a local
un-gitted planning document, for the full breakdown). What's left is only what was
deliberately out of scope for this portfolio project from the start: multi-tenancy, a
public customer-facing status page, webhooks for third-party integration.
