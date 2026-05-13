# Core Banking Lite

> Enterprise-grade banking platform — Java 21 + Spring Boot 3 REST API with a Next.js 15 frontend. Built to production standards: JWT auth, idempotent transfers, Redis caching, rate limiting, structured logging, Testcontainers integration tests, and a full CI/CD pipeline on GitHub Actions.

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
└──────────────────────────────────────────────────────────────┘
```

### Backend Package Layout

```
com.corebanking/
├── audit/          # AOP aspect — logs every controller call with timing
├── config/         # CacheConfig (RedisCacheManager), OpenAPI
├── exception/      # GlobalExceptionHandler, ErrorCode enum (CBL-XXX)
├── filter/         # CorrelationIdFilter, RateLimitFilter (Bucket4j)
├── security/       # SecurityConfig, JwtAuthFilter, JWT service
├── shared/         # PagedResponse, BaseEntity
└── modules/
    ├── auth/       # Login, refresh token
    ├── user/       # Spring Security UserDetails integration
    ├── customer/   # CRUD + soft-delete + status lifecycle
    ├── account/    # Multi-currency accounts (PEN/USD), movements
    ├── transfer/   # Idempotent inter-account transfers, daily limit
    └── dashboard/  # Aggregate KPIs + time-series stats (cached 1 min)
```

---

## Key Engineering Decisions

| Decision | Rationale |
|----------|-----------|
| **Idempotency key on transfers** | Clients send a UUID per request; the server stores it with a unique constraint — guarantees exactly-once processing on network retries |
| **Atomic debit + credit** | Both account movements run inside a single `@Transactional` boundary — no partial state is ever persisted |
| **Daily transfer limit** | Configurable via env var `TRANSFER_DAILY_LIMIT` (default 50 000 PEN); checked against a native aggregate query before executing |
| **Redis cache with per-TTL** | Dashboard summary cached 1 min; customer lookups cached 10 min; `@CacheEvict` on every mutation |
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
| API Docs | SpringDoc OpenAPI 3 / Swagger UI |
| Logging | Logback + Logstash encoder (JSON in prod) |
| Testing | JUnit 5, Mockito, Testcontainers, Vitest, Testing Library |
| Build | Gradle 8 (Kotlin DSL), npm |
| CI/CD | GitHub Actions (test → lint → docker push to GHCR) |
| Containers | Docker, Docker Compose |

---

## Modules

### `customers`
Full lifecycle management: create, update, soft-delete, status transitions (`ACTIVO → INACTIVO → SUSPENDIDO`). Duplicate document/email validation. `@Cacheable` on `findById`, `@CacheEvict` on mutations. JPA Specification for dynamic filtering.

### `accounts`
Multi-currency accounts (PEN / USD) of types `AHORRO`, `CORRIENTE`, `CTS`. Every balance change creates an immutable `AccountMovement` record with a typed `MovementType` enum. Soft-delete via `deleted` flag.

### `transfers`
Idempotent inter-account transfers. Flow:
1. Validate idempotency key (return existing if duplicate)
2. Guard against self-transfers
3. Check daily limit via aggregate DB query
4. Atomic `debit + credit` in one transaction
5. Persist `Transfer` record linking both movements

### `dashboard`
Aggregate KPIs computed from live DB queries (customer counts, total balances by currency, transfer volume today/this month). Time-series data for the last N days via a native PostgreSQL query. Results cached in Redis for 1 minute.

### `auth`
Login → JWT access token (24 h) + refresh token (7 days). Refresh endpoint rotates both tokens. Stateless — no server-side session.

---

## Security

- **Authentication**: Bearer JWT validated on every request by `JwtAuthFilter`
- **Authorization**: `@EnableMethodSecurity` + `@PreAuthorize` on sensitive operations
- **HSTS**: 1 year, including subdomains
- **CSP**: `default-src 'self'; frame-ancestors 'none'`
- **X-Frame-Options**: `DENY`
- **Rate limiting**: 100 req/min per IP (Bucket4j token bucket)
- **Password storage**: BCrypt
- **Error responses**: Never expose stack traces or internal messages (`include-message: never`)

---

## Testing

```
backend/
  unit tests       → JUnit 5 + Mockito + H2 in-memory
  integration tests → Testcontainers (postgres:16-alpine) + Spring MVC test

frontend/
  unit tests       → Vitest + Testing Library (jsdom)
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
# Set JWT_SECRET to a 256-bit hex string
```

### 2. Start infrastructure
```bash
docker compose up postgres redis -d
```

### 3. Backend
```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=dev'
# API → http://localhost:8080/api
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
| `TRANSFER_DAILY_LIMIT` | `50000.00` | Daily transfer cap per account |
| `SPRING_PROFILES_ACTIVE` | `dev` | `dev` / `prod` |
