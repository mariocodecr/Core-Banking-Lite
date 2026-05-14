# Core Banking Lite

> Enterprise-grade banking platform — Java 21 + Spring Boot 3 REST API with a Next.js 15 frontend. Built to production standards: JWT auth, role-based access (ADMIN / ADVISOR / AUDITOR / CLIENT), idempotent transfers, multi-currency exchange rates (BCCR), ETF/mutual fund investments with real market data (Alpha Vantage), Redis caching, rate limiting, structured logging, Testcontainers integration tests, and a full CI/CD pipeline on GitHub Actions.

---

## Architecture Overview

```
┌──────────────────────────────────────────────────────────────┐
│                        Client (Browser)                       │
│              Next.js 15  ·  React 19  ·  TypeScript          │
│         TanStack Query  ·  React Hook Form  ·  Zod           │
└────────────────────────────┬─────────────────────────────────┘
                             │ HTTPS + X-Correlation-ID
                             ▼
┌──────────────────────────────────────────────────────────────┐
│                  Spring Boot 3  (Java 21)                     │
│                                                              │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────────┐  │
│  │  JWT Filter │  │ RateLimit    │  │ CorrelationId      │  │
│  │  (Stateless)│  │ Filter (B4j) │  │ Filter (MDC)       │  │
│  └─────────────┘  └──────────────┘  └────────────────────┘  │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐  │
│  │               REST Controllers (OpenAPI)               │  │
│  └────────────────────────┬───────────────────────────────┘  │
│                           │  AuditLogAspect (@AOP)           │
│  ┌────────────────────────▼───────────────────────────────┐  │
│  │                    Service Layer                        │  │
│  │  @Cacheable / @CacheEvict  ·  @Transactional           │  │
│  └────────┬────────────────────────────────┬──────────────┘  │
│           │                                │                  │
│  ┌────────▼────────┐             ┌─────────▼──────────────┐  │
│  │   PostgreSQL 16 │             │      Redis 7           │  │
│  │   (JPA/Hibernate│             │  (Cache · TTL per-key) │  │
│  └─────────────────┘             └────────────────────────┘  │
│                                           │                   │
│                              ┌────────────▼───────────────┐  │
│                              │  Alpha Vantage API         │  │
│                              │  (market data · 1h TTL)    │  │
│                              └────────────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

### Backend Package Layout

```
com.corebanking/
├── audit/          # AOP aspect — logs every controller call with timing
├── config/         # CacheConfig (RedisCacheManager), SchedulingConfig, OpenAPI
├── exception/      # GlobalExceptionHandler, ErrorCode enum (CBL-XXX)
├── filter/         # CorrelationIdFilter, RateLimitFilter (Bucket4j)
├── security/       # SecurityConfig, JwtAuthFilter, JWT service
├── shared/         # PagedResponse<T>, BaseEntity
└── modules/
    ├── auth/         # Login, refresh token
    ├── user/         # Spring Security UserDetails integration
    ├── customer/     # CRUD + soft-delete + status lifecycle
    ├── account/      # Multi-currency accounts (CRC/USD/EUR), movements
    ├── transfer/     # Idempotent inter-account transfers, daily limit
    ├── exchangerate/ # Live rates from BCCR (Costa Rica Central Bank) + fallback APIs
    ├── investment/   # ETF / mutual fund portfolios, market data, orders
    └── dashboard/    # Aggregate KPIs + time-series stats (cached 1 min)
```

---

## Modules

### `customers`
Full lifecycle management: create, update, soft-delete, status transitions (`ACTIVO → INACTIVO → SUSPENDIDO`). Duplicate document/email validation. `@Cacheable` on `findById`, `@CacheEvict` on mutations. JPA Specification for dynamic filtering.

### `accounts`
Multi-currency accounts (CRC / USD / EUR) of types `AHORRO`, `CORRIENTE`, `EMPRESARIAL`. Every balance change creates an immutable `AccountMovement` record with a typed `MovementType` enum (`DEPOSITO`, `RETIRO`, `TRANSFERENCIA_ENTRADA`, `TRANSFERENCIA_SALIDA`, `COMPRA_INVERSION`, `VENTA_INVERSION`). `GET /v1/accounts/me` returns the authenticated user's own accounts (all roles including CLIENT).

### `transfers`
Idempotent inter-account transfers with multi-currency support. Flow:
1. Validate idempotency key (return existing if duplicate)
2. Guard against self-transfers
3. Check daily limit via aggregate DB query
4. Resolve exchange rate (BCCR) if currencies differ
5. Atomic `debit + credit` in one transaction
6. Persist `Transfer` record linking both movements

`GET /v1/transfers/me` returns paginated transfers involving the authenticated user's own accounts (all roles including CLIENT).

### `exchangerate`
Live exchange rates sourced from BCCR (Banco Central de Costa Rica) for CRC pairs, and open.er-api.com for EUR. Last known rate is persisted to the DB as a fallback when external APIs are unavailable. Rates are cached in Redis.

### `investments`
Full investment module for ETFs and mutual funds:

- **Instruments**: 20 pre-seeded instruments (SPY, QQQ, VTI, ARKK, GLD, BND and more). Prices sourced from Alpha Vantage API, cached in Redis for 1 hour. `lastPrice` persisted to DB as fallback when the API is rate-limited.
- **Portfolios**: one portfolio per USD account. Tracks positions with weighted average cost (WAC).
- **Orders**: `BUY` and `SELL` orders execute atomically — account debit/credit + position update in a single transaction. Mutual fund orders are queued as `PENDING` and processed asynchronously via `@Scheduled`.
- **Combined portfolio**: `GET /v1/investments/portfolio` aggregates all positions across all portfolios grouped by symbol with WAC — no account selection required.
- **Summary**: `GET /v1/investments/summary` returns aggregate KPIs (total invested, current value, P&L, active portfolios, total positions).

### `dashboard`
Aggregate KPIs computed from live DB queries (customer counts, total balances by currency, transfer volume today/this month). Time-series data for the last N days via a native PostgreSQL query. Results cached in Redis for 1 minute.

Clients with the `CLIENT` role see a personal dashboard (their own balances, investment P&L, recent transfers) instead of the system-wide admin dashboard.

### `auth`
Login → JWT access token (24 h) + refresh token (7 days). Refresh endpoint rotates both tokens. Stateless — no server-side session.

---

## Role-Based Access

| Feature | ADMIN | ADVISOR | AUDITOR | CLIENT |
|---------|:-----:|:-------:|:-------:|:------:|
| System dashboard (all customers/accounts) | ✓ | ✓ | ✓ | — |
| Personal dashboard (own balances + P&L) | — | — | — | ✓ |
| Customer management | ✓ | ✓ | read | — |
| All accounts | ✓ | ✓ | ✓ | — |
| Own accounts (`/me`) | ✓ | ✓ | ✓ | ✓ |
| Create / freeze / close accounts | ✓ | ✓ | — | — |
| All transfers | ✓ | ✓ | ✓ | — |
| Own transfers (`/me`) | ✓ | ✓ | ✓ | ✓ |
| Create transfers | ✓ | ✓ | — | ✓ |
| Investments (trade) | ✓ | ✓ | — | ✓ |
| Investment summary (system-wide) | ✓ | ✓ | ✓ | — |
| Audit log | ✓ | ✓ | ✓ | — |

---

## Key Engineering Decisions

| Decision | Rationale |
|----------|-----------|
| **Idempotency key on transfers** | Clients send a UUID per request; the server stores it with a unique constraint — guarantees exactly-once processing on network retries |
| **Atomic debit + credit** | Both account movements run inside a single `@Transactional` boundary — no partial state is ever persisted |
| **Daily transfer limit** | Configurable via env var `TRANSFER_DAILY_LIMIT` (default 500 000); checked against a native aggregate query before executing |
| **Redis cache with per-key TTL** | Dashboard summary cached 1 min; customer lookups cached 10 min; market prices cached 1 h; `@CacheEvict` on every mutation |
| **Alpha Vantage + DB fallback** | Market prices fetched from Alpha Vantage (free tier: 25 req/day) and cached in Redis for 1 h. On startup, `InstrumentDataInitializer` seeds `lastPrice` for all instruments so the system never returns an error when the API is rate-limited |
| **Combined portfolio (WAC)** | `GET /v1/investments/portfolio` aggregates positions across all user portfolios by symbol using weighted average cost — clients see a unified view without selecting an account |
| **Multi-currency exchange rates** | BCCR is the primary source for CRC pairs; open.er-api.com handles EUR. Last known rate is persisted to DB so transfers never fail due to a temporary API outage |
| **`@Jacksonized` on `@Builder` DTOs** | Lombok `@Builder` removes the default constructor needed by Jackson; `@Jacksonized` generates the `@JsonDeserialize(builder=...)` metadata |
| **Token-bucket rate limiting** | 100 req/min per IP in-memory (Bucket4j); comment in code documents the Redis `ProxyManager` upgrade path for multi-instance |
| **`X-Correlation-ID` propagation** | Set by `CorrelationIdFilter` at `Order(MIN_VALUE)`, stored in MDC, returned as a response header — every log line carries the trace ID |
| **Structured JSON logs in prod** | `logback-spring.xml` uses `LogstashEncoder` on profile `prod` — output is parseable by any log aggregator (ELK, CloudWatch, Datadog) |
| **Testcontainers for ITs** | Integration tests spin up a real `postgres:16-alpine` container; shared static container avoids restart overhead per test class |
| **Next.js standalone output** | `output: "standalone"` produces a self-contained `server.js` — Docker image needs no `node_modules` volume |
| **`next/dynamic` for Recharts** | Charts are client-only heavy bundles (~150 kB); lazy-loading them keeps the initial page JS small |

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | Next.js 15 (App Router), React 19, TypeScript |
| Styling | Tailwind CSS, shadcn/ui |
| State / Data | TanStack Query v5, React Hook Form, Zod |
| Charts | Recharts (lazy-loaded) |
| Backend | Java 21, Spring Boot 3.3, Spring Security |
| Database | PostgreSQL 16, Spring Data JPA / Hibernate |
| Cache | Redis 7, Spring Cache (`RedisCacheManager`) |
| Rate Limiting | Bucket4j 8 (token-bucket algorithm) |
| Auth | JWT (JJWT 0.12), BCrypt, stateless sessions |
| Market Data | Alpha Vantage REST API (ETFs + mutual funds) |
| Exchange Rates | BCCR API (CRC), open.er-api.com (EUR) |
| API Docs | SpringDoc OpenAPI 3 / Swagger UI |
| Logging | Logback + Logstash encoder (JSON in prod) |
| Testing | JUnit 5, Mockito, Testcontainers, Vitest, Testing Library |
| Build | Gradle 8 (Kotlin DSL), npm |
| CI/CD | GitHub Actions (test → lint → docker push to GHCR) |
| Containers | Docker, Docker Compose |

---

## Testing

```
backend/
  unit tests        → JUnit 5 + Mockito + H2 in-memory
  integration tests → Testcontainers (postgres:16-alpine) + Spring MVC test

frontend/
  unit tests        → Vitest + Testing Library (jsdom)
```

Run all backend tests with coverage:
```bash
cd backend && ./gradlew test jacocoTestReport
```

Run frontend tests:
```bash
cd frontend && npm test
```

Coverage report: `backend/build/reports/jacoco/test/html/index.html`

---

## Quick Start

### Prerequisites
- Java 21, Docker & Docker Compose, Node.js 20+

### 1. Environment setup
```bash
cp .env.example .env
# Required: JWT_SECRET (256-bit hex string), ALPHA_VANTAGE_API_KEY
```

### 2. Start infrastructure
```bash
docker compose up postgres redis -d
```

### 3. Backend
```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=dev'
# API     → http://localhost:8080/api
# Swagger → http://localhost:8080/api/swagger-ui.html
```

### 4. Frontend
```bash
cd frontend && npm install && npm run dev
# App → http://localhost:3000
```

### Full stack (Docker)
```bash
docker compose --profile full up -d
```

---

## Default Users

| Email | Password | Role |
|-------|----------|------|
| `admin@corebanking.com` | `admin123` | ADMIN |
| `advisor@corebanking.com` | `advisor123` | ADVISOR |
| `auditor@corebanking.com` | `auditor123` | AUDITOR |
| `client@corebanking.com` | `client123` | CLIENT |

---

## CI/CD Pipeline

```
Push / PR
    │
    ├── ci-backend.yml
    │     └── Unit tests → Integration tests (Testcontainers) → JaCoCo report
    │
    ├── ci-frontend.yml
    │     └── tsc --noEmit → ESLint → Vitest
    │
    └── docker-publish.yml  (main / develop / tags only)
          └── Build → Push to GHCR with layer cache (type=gha)
```

Images are published to `ghcr.io/<owner>/core-banking-lite-backend` and `-frontend`.

---

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/corebanking_dev` | PostgreSQL JDBC URL |
| `DATABASE_USERNAME` | `corebanking` | DB user |
| `DATABASE_PASSWORD` | `corebanking123` | DB password |
| `JWT_SECRET` | *(must be set)* | 256-bit hex key |
| `JWT_EXPIRATION` | `86400000` | Access token TTL (ms) |
| `JWT_REFRESH_EXPIRATION` | `604800000` | Refresh token TTL (ms) |
| `REDIS_HOST` | `localhost` | Redis hostname |
| `REDIS_PORT` | `6379` | Redis port |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | Comma-separated allowed origins |
| `TRANSFER_DAILY_LIMIT` | `500000.00` | Daily transfer cap per account |
| `ALPHA_VANTAGE_API_KEY` | *(must be set in prod)* | Alpha Vantage API key for market data |
| `SPRING_PROFILES_ACTIVE` | `dev` | `dev` / `prod` |
