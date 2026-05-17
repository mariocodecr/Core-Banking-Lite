# Interview Preparation — Core Banking Lite

## CV Bullets

> Paste these under your project section. Pick 4–5 that match the job description.

- Built an **enterprise banking REST API** with Java 21 + Spring Boot 3 handling customers, multi-currency accounts, and inter-account transfers with **idempotency guarantees** (unique constraint + early-return on duplicate key)
- Implemented **exactly-once transfer semantics** via idempotency keys and atomic `@Transactional` debit + credit, preventing duplicate charges on network retries
- Designed a **per-cache TTL strategy** with Redis and Spring Cache — dashboard KPIs cached 1 min, customer lookups 10 min; cache eviction on every mutation via `@CacheEvict`
- Added **Bucket4j token-bucket rate limiting** (100 req/min per IP) and security headers (HSTS, CSP, `X-Frame-Options`) to harden the API surface
- Propagated **X-Correlation-ID** through every layer (filter → MDC → response header) enabling distributed tracing without an external tracing system
- Wrote **AOP-based audit logging** that intercepts every controller call, records method signature, execution time, and exception details — zero coupling to business logic
- Configured **structured JSON logging** with Logstash encoder (prod) and colored console (dev) using profile-scoped `logback-spring.xml`
- Built **integration tests with Testcontainers** (real PostgreSQL 16) using a shared static container and `@DynamicPropertySource` — tests run in GitHub Actions CI without any external DB
- Achieved **>80% test coverage** (JUnit 5 + Mockito) verified by JaCoCo with exclusions for generated code (MapStruct, Lombok DTOs)
- Shipped a **Next.js 15 frontend** with TanStack Query, React Hook Form + Zod validation, and Recharts dashboard lazy-loaded via `next/dynamic` to reduce initial bundle size
- Containerized with **multi-stage Docker builds** (Gradle → JRE Alpine for backend; node deps → builder → Next.js standalone for frontend); images published to GHCR via GitHub Actions

---

## STAR Stories

### 1. Idempotent Transfers (favorite "reliability" question)

**Situation**: A banking transfer endpoint that gets retried on network timeout would charge the customer twice — a critical correctness bug in any financial system.

**Task**: Implement exactly-once semantics without requiring a distributed transaction manager or saga orchestrator.

**Action**:
- Added an `idempotency_key` column with a `UNIQUE` constraint on the `transfers` table
- On every transfer request, the service first queries for an existing record by key — if found, returns the existing `Transfer` immediately without re-executing
- The actual debit + credit runs inside a single `@Transactional` method: debit source, credit destination, save `Transfer` — all three writes commit atomically or all roll back
- Enforced a configurable daily limit via a `SUM` aggregate query checked before the transaction opens

**Result**: Zero duplicate charges on network retries. The pattern mirrors what Stripe and PayPal use in their payment APIs — a direct talking point with any payments-focused interviewer.

---

### 2. Redis Caching with @Builder DTOs (favorite "debugging" question)

**Situation**: After adding Redis cache to the dashboard, deserialization started throwing `InvalidDefinitionException: no suitable constructor found`. The endpoint worked fine without cache.

**Task**: Fix deserialization without changing the caching strategy or removing `@Builder`.

**Action**:
- Identified that Lombok `@Builder` suppresses the default no-args constructor, which Jackson requires to deserialize JSON back into an object retrieved from Redis
- Discovered Lombok's `@Jacksonized` annotation — it generates `@JsonDeserialize(builder = DashboardSummaryResponse.DashboardSummaryResponseBuilder.class)` automatically, telling Jackson to use the builder pattern instead of a constructor
- One annotation fixed the issue; no boilerplate, no custom deserializer

**Result**: Dashboard cache works correctly end-to-end. This is a subtle Lombok + Jackson interaction that trips up most mid-level developers — knowing the root cause signals deep library understanding.

---

### 3. AOP Audit Logging (favorite "cross-cutting concerns" question)

**Situation**: The team needed request/response audit logs on every API endpoint without touching 20+ controller methods or introducing logging boilerplate in business logic.

**Task**: Add structured audit logging that captures method, timing, and exception info — fully decoupled from controllers.

**Action**:
- Created `AuditLogAspect` with a single `@Around("within(@RestController *)")` pointcut
- The advice records the method signature and start time, calls `pjp.proceed()`, then logs either a success line with elapsed ms or an error line with exception class and message
- The `correlationId` is automatically included in every log line because `CorrelationIdFilter` already placed it in MDC before any controller was invoked
- Zero changes to existing controllers — dropped the aspect into the `audit` package and it picked up all endpoints automatically

**Result**: Complete request tracing with no controller coupling. Adding a new endpoint gets audited for free.

---

### 4. Testcontainers Integration Tests (favorite "testing strategy" question)

**Situation**: Unit tests with H2 in-memory gave confidence for business logic, but missed PostgreSQL-specific behavior: native queries, dialect differences, and constraint violations.

**Task**: Add integration tests against a real PostgreSQL instance that run reliably in CI without managing an external database.

**Action**:
- Introduced `BaseIntegrationTest` using Testcontainers with a `static` `PostgreSQLContainer` — declared once, shared across all IT classes in the same JVM, avoiding a container restart per test
- Used `@DynamicPropertySource` to override `spring.datasource.*` with the container's ephemeral host/port at test startup
- Added `TESTCONTAINERS_RYUK_DISABLED=true` in the GitHub Actions workflow to avoid Docker-in-Docker permission issues
- Wrote seed helpers (`loginAndGetToken`, `seedCustomer`) in the base class so IT subclasses stay focused on the scenario, not setup

**Result**: Integration tests now catch real SQL issues (e.g., dialect-specific aggregate casting, constraint violations) that unit tests would miss — and they run in under 90 seconds in CI.

---

### 5. Security Headers (favorite "security hardening" question)

**Situation**: The API passed functional requirements but had no HTTP security headers — failing standard security scans (OWASP ZAP, Mozilla Observatory).

**Task**: Add defense-in-depth headers without breaking existing functionality (especially CORS for the frontend).

**Action**:
- Configured Spring Security's `headers()` DSL: HSTS (1 year, includeSubDomains), `X-Frame-Options: DENY`, CSP (`default-src 'self'; frame-ancestors 'none'`), and Permissions-Policy
- Added Bucket4j token-bucket rate limiting (100 req/min per IP) as an `OncePerRequestFilter`, with `X-Rate-Limit-Retry-After-Seconds: 60` on 429 responses
- Client IP resolved from `X-Forwarded-For` to work correctly behind a reverse proxy or load balancer
- Kept error responses clean: `include-message: never` in `application.yml` — stack traces never leak to clients

**Result**: API passes OWASP Top 10 checks for security misconfigurations. Rate limiting prevents brute-force attacks on the auth endpoint with no library overhead beyond Bucket4j core.

---

## Technical Deep-Dives (expect these questions)

**Q: Why stateless JWT instead of sessions?**
Stateless tokens scale horizontally with no shared session store. The tradeoff is you can't revoke a token before expiry — mitigated here by keeping access token TTL short (24 h) and using refresh tokens (7 days) that can be invalidated server-side.

**Q: How does the daily transfer limit work under concurrent load?**
The limit check reads the current day's sum and then opens the transaction to debit. Under very high concurrency, two threads could both read "limit not exceeded" and both proceed. A production-grade fix is a `SELECT ... FOR UPDATE` on the account row (pessimistic locking) or an optimistic lock with `@Version`. The current implementation is suitable for low-to-medium traffic; the code comment documents this explicitly.

**Q: Why Bucket4j in-memory instead of Redis-backed?**
In-memory is simpler to set up and sufficient for a single-instance deployment. The `RateLimitFilter` comment explicitly calls out the upgrade path: replace `ConcurrentHashMap<String, Bucket>` with Bucket4j's `RedisProxyManager` — the rest of the filter stays identical.

**Q: What's the difference between `@Cacheable` on `findById` and caching `findAll`?**
`findById` has a stable, single-valued key (the UUID) — cache hit/miss and eviction are trivial. `findAll` with dynamic filters and pagination produces a different cache key per query combination, making effective caching complex. The implementation caches individual lookups (high read frequency, stable data) and leaves list queries to hit the DB — the right tradeoff for a read-heavy customer profile use case.
