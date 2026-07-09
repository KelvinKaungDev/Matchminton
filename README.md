# Matchminton

REST API backend for a badminton club/tournament manager: organizers create groups, run competition sessions with live-scored courts, and manage bench/rotation-based practice sessions. Built with Spring Boot + PostgreSQL.

## Tech stack

- **Java 21**, **Spring Boot 4.0.6**
- **PostgreSQL** via Spring Data JPA / Hibernate
- **Spring Security** with JWT (`jjwt`) for stateless auth
- **springdoc-openapi** for Swagger UI
- Maven (wrapper included, no local install needed)

## Architecture

Layered per feature: `Controller → Service (interface + impl) → Repository → Model/Entity`, with request/response DTOs (entities are never returned directly), custom exceptions mapped to HTTP status by a global `@ControllerAdvice`, and constructor injection throughout. See `SKILL.md` for the full conventions this codebase follows.

## Features

- **Auth** — register/login, JWT bearer tokens
- **Users** — profile + subscription tier (`free`/`pro`)
- **Groups** — organizer-run clubs (skill level, photo upload, search/filter)
- **Competition sessions** — fixed-bracket tournaments: a session has courts, each court runs games (best-of-N, points-to-win, deuce rule), players are assigned to a court + team. Court lookup by a 4-character code and score-related endpoints are public so judges/spectators can view and score without logging in — only session/court/roster setup requires the organizer's JWT.
- **Rotation** — bench/waiting/playing/done scheduling for casual sessions: organizer configures court count and rounds-per-player, adds a player pool, and the backend runs the matching algorithm (least-rounds-played first) to auto-assign courts each round, including substitution when a player leaves mid-round.

## Getting started

### Prerequisites

- JDK 21
- PostgreSQL running locally (or use Docker Compose, see below)

### Local (no Docker)

1. Create a database:
   ```
   createdb badminton-manager
   ```
2. Run the app (defaults to `localhost:5432`, username from your OS user, no password — override via env vars below if needed):
   ```
   ./mvnw spring-boot:run
   ```
3. API is live at `http://localhost:8081`. Swagger UI: `http://localhost:8081/swagger-ui.html`.

### Environment variables

All have working local defaults (see `src/main/resources/application.properties`); override for other environments:

| Variable | Default |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/badminton-manager` |
| `DB_USERNAME` | current OS user |
| `DB_PASSWORD` | *(empty)* |
| `JWT_SECRET` | dev-only placeholder — **set a real 32+ byte secret in production** |
| `JWT_EXPIRATION_MS` | `86400000` (24h) |

### Docker Compose

Runs Postgres + the app together:

```
docker compose up --build
```

App on `http://localhost:8081`, Postgres on `5432`. Config is in `docker-compose.yml` (dev-only credentials — don't reuse them anywhere real).

## API overview

Full interactive docs at `/swagger-ui.html` (JWT bearer auth supported directly in the UI). Grouped by resource, all under `/api`:

| Resource | Base path | Notes |
|---|---|---|
| Auth | `/auth` | `POST /register`, `POST /login` — public |
| Users | `/users` | CRUD |
| Groups | `/groups` | CRUD, `/active`, `/search?name=`, `/skill-level/{level}`, `/organizer/{id}`, `POST /{id}/photo` (multipart) |
| Competition sessions | `/competition-sessions` | CRUD, `/active`, `/organizer/{id}`, `PATCH /{id}/finish` |
| Competition courts | `/competition-courts` | CRUD, `/session/{id}`, `/code/{courtCode}` (public), `PATCH /{id}/finish?winner=` (public) |
| Games | `/games` | CRUD, `/court/{id}` (public), `PATCH /{id}/finish?winner=` (public) |
| Players (competition) | `/players` | CRUD, `/court/{id}` (public read) — bound to a court + team |
| Rotation sessions | `/rotation-sessions` | `/organizer/{id}` (fetch-or-create), config update, `/start`, `/fill-empty-courts`, `/mark-all-bench`, `/reset`, `/screen` |
| Rotation players | `/rotation-players` | create/update/delete, `/status`, `/leave` (auto-substitute) |
| Rotation courts | `/rotation-courts` | `/complete`, `/refill`, `/swap` |

All endpoints require a JWT bearer token except `/api/auth/**` and the routes marked *public* above (judge/spectator access to competition scoring).

## Testing

```
./mvnw test
```

Service-layer tests (Mockito) and controller-layer tests (`@WebMvcTest` + MockMvc, security filters disabled in the slice) live under `src/test/java`, mirroring the main package structure.

## Deployment

- `Dockerfile` — multi-stage build (Maven build → JRE runtime image), exposes `8081`
- `.github/workflows/ci.yml` — runs `./mvnw verify` against a Postgres service container on every push/PR to `main`
- Use real environment variables for `DB_URL`/`DB_USERNAME`/`DB_PASSWORD`/`JWT_SECRET` in any deployed environment — never the Docker Compose dev defaults
