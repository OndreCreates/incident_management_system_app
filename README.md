# Incident Management System

Backend + admin panel pro správu incidentů: explicitní state machine, SLA sledování a
append-only audit trail. Třetí projekt v portfoliu — po `identity_server_app` (auth) a
`notification_center_app` (async doručování) — demonstruje doménu **stavové byznys logiky
a návrhu workflow**.

Není to klon PagerDuty a feature parity nebyl cíl. Cílem bylo navrhnout a obhájit state
machine s reálnými byznys pravidly (SLA, audit trail) pod tlakem otázek na pohovoru —
ne postavit co nejvíc featur.

## Architektura

```
                        ┌─────────────────────┐
  browser ─────────────▶│  admin-panel (:3001) │
                        │  Next.js, OAuth2/PKCE│
                        └──────────┬───────────┘
                                   │ Bearer token
                                   ▼
                        ┌─────────────────────┐        ┌──────────────────────┐
                        │   app (:8080)        │───────▶│ identity_server_app   │
                        │  Spring Boot 3       │  JWKS  │  (:9000, samostatný   │
                        │  OAuth2 Resource Srv  │        │  docker-compose stack)│
                        └──────────┬───────────┘        └──────────────────────┘
                                   │
                                   ▼
                        ┌─────────────────────┐
                        │   mysql (:3308)       │
                        └─────────────────────┘
```

- **State machine**: ruční `Map<Status, Set<Status>>` v `IncidentTransitionService`, ne
  knihovna — 6 stavů, v podstatě lineární s jednou reopen větví neospravedlňuje Spring
  State Machine.
- **Auth**: tahle služba nevydává ani nespravuje credentials. JWT vydává
  `identity_server_app`, validuje se tady proti jeho JWKS endpointu. `sub` claim nese
  uživatelův email (identity_server_app nedává do tokenu žádné numerické ID) — proto je
  externí user ID napříč touto službou `VARCHAR`, ne `BIGINT`.
- **Audit trail**: `incident_timeline` je append-only. Žádný UPDATE/DELETE proti té
  tabulce nikdy.
- **Admin panel**: server-first Next.js (Server Components + Server Actions) — access
  token žije jen v httpOnly cookii na serveru, do prohlížeče se nikdy nedostane.

## Rychlý start

### Docker Compose (celý stack)

Vyžaduje běžící `identity_server_app` (vlastní `docker compose up` v jeho adresáři —
poskytuje JWKS a login flow).

```bash
# 1. identity_server_app (v jeho vlastním adresáři)
cd ../Identity_server_app && docker compose up -d

# 2. tenhle stack (MySQL + backend + admin panel)
cd ../Incident_management_system_app && docker compose up -d --build
```

Po naběhnutí:

- Admin panel: http://localhost:3001
- API: http://localhost:8080/api/v1
- Swagger UI: http://localhost:8080/swagger-ui.html
- Identity server login: http://localhost:9000/login (seed účet
  `admin@identity-server.dev` / `admin123`)

### Bez Dockeru (lokální vývoj)

```bash
docker compose up -d mysql          # jen databáze
mvn spring-boot:run                 # backend na :8080

cd admin-panel
cp .env.example .env.local          # a doplň hodnoty
npm install
npm run dev -- -p 3001              # admin panel na :3001
```

### Testy

```bash
mvn test                            # 47 testů: unit (state machine matrix) + integrační (proti reálné MySQL)
cd admin-panel && npm run build     # type-check + build všech routes
```

## Verifikační kroky (E2E)

1. `docker compose up -d --build` v tomhle adresáři, s běžícím `identity_server_app`
   vedle — celý stack naběhne bez chyby (`docker ps` ukáže tři healthy kontejnery).
2. Přihlas se přes admin panel (http://localhost:3001) přes login flow
   `identity_server_app` (první přihlášení vyžaduje nastavení TOTP — MFA je vynucené pro
   všechny uživatele, ne jen volitelné).
3. Vytvoř incident, projdi ho celým happy path: Created → Assigned → Investigating →
   Mitigated → Resolved → Closed.
4. Zkus nevalidní přechod (např. Created → Closed přímo) přes `/swagger-ui.html` nebo
   curl — ověř 409 s tělem `{"error":"INVALID_TRANSITION","allowed":[...]}`.
5. Vytvoř incident se severity `CRITICAL`, over jeho `sla_deadline` v databázi do
   minulosti (`UPDATE incident SET sla_deadline = NOW() - INTERVAL 1 HOUR WHERE id = ?`),
   počkej do 60s na scheduled job, ověř že `sla_breached` sklopí na `true` a projeví se na
   dashboardu.
6. Přidej komentář, ověř že se objeví v timeline feedu na detailu incidentu.
7. `curl -i http://localhost:8080/api/v1/incidents` bez `Authorization` hlavičky — ověř
   401.
8. `mvn test` — všechny testy zelené.

## Zajímavá návrhová rozhodnutí

- **Assignment jako samostatný audit fakt.** `assigned_user_id` se mění přes
  `IncidentAssignmentService`, ne uvnitř `IncidentTransitionService` — přiřazení a
  stavový přechod jsou nezávislé koncepty (potvrzuje to i state machine: reassignment se
  modeluje jako přechod zpátky do `ASSIGNED`, nezávisle na tom, kdo byl přiřazený
  předtím), takže mají vlastní `EventType.ASSIGNMENT` timeline záznam.
- **`open-in-view: false` + `LEFT JOIN FETCH`.** Timeline dotaz explicitně fetchuje
  přidruženou `IncidentComment`, aby `TimelineEntryResponse` mohla ukázat obsah
  komentáře bez `LazyInitializationException` mimo request-scoped transakci.
- **User ID jako email, ne BIGINT.** Zjištěno až při napojování na `identity_server_app`
  (Fáze 1D) — JWT `sub` claim nese email, ne numerické `AppUser.id`. Viz `V2__user_id_as_email.sql`.

## Známá omezení (záměrná, ne přehlédnutá)

- **Tvrdá runtime závislost na `identity_server_app`.** Bez JWKS endpointu appka
  nenaběhne do funkčního stavu (mutující endpointy vrátí 401). Přijatelné pro portfolio
  demo; v reálném multi-team prostředí by to chtělo token cache / circuit breaking.
- **Žádný automatizovaný E2E test s reálně vydaným tokenem.** MFA je vynucené pro
  všechny přihlášení do `identity_server_app`, takže plný authorization_code+PKCE flow
  nejde skriptovat bez lidského TOTP kroku — pokryto manuálním ověřením výše, integrační
  testy backendu používají mockovaný JWT (`SecurityMockMvcRequestPostProcessors`).
- **Eskalace, týmy, postmortem modul, konfigurovatelné SLA** — Fáze 2/3, viz
  `ROADMAP.md` (lokální, negitovaný plánovací dokument).

## Roadmapa — co dál

- Fáze 2: eskalace (přes `notification_center_app`), přiřazení na tým, postmortem modul
- Fáze 3: dashboard analytika, konfigurovatelné SLA politiky, CI/CD
