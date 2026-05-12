# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

### Backend (Spring Boot 3 + Gradle)
```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=dev'           # Dev server (port 8080)
./gradlew test                                                     # All tests
./gradlew test --tests "com.corebanking.*ServiceTest"             # Single test class
./gradlew jacocoTestReport                                         # Coverage report (build/reports/jacoco)
./gradlew clean build -x test                                      # Build JAR, skip tests
```

### Frontend (Next.js 15)
```bash
cd frontend
npm run dev        # Dev server (port 3000)
npm run lint       # ESLint
npm run format     # Prettier
```

### Docker
```bash
docker compose up postgres -d            # Database only
docker compose --profile full up -d     # Full stack
docker compose down -v                  # Stop + remove volumes
```

## Architecture

### Backend Package Structure
```
backend/src/main/java/com/corebanking/
├── CoreBankingApplication.java
├── audit/           # AuditableEntity — base class for all entities (JPA auditing)
├── config/          # OpenApiConfig, JpaAuditingConfig, WebConfig (CORS)
├── exception/       # GlobalExceptionHandler, ErrorCode enum, BusinessException, ResourceNotFoundException
├── shared/          # PagedResponse<T> — generic paged wrapper
└── modules/         # Domain modules added per phase
    └── {module}/
        ├── controller/   # @RestController, request mapping, Swagger annotations
        ├── service/      # Interface + Impl — business logic lives here
        ├── repository/   # Spring Data JPA repositories
        ├── entity/       # JPA entities, must extend AuditableEntity
        ├── dto/          # Request/Response DTOs — never expose entities
        └── mapper/       # MapStruct interfaces — never write manual mappers
```

### Frontend Source Structure
```
frontend/src/
├── app/             # Next.js App Router — pages and layouts
│   └── (auth)/     # Route group for unauthenticated pages
├── components/ui/   # Shadcn/ui components — add via CLI, never write manually
├── features/        # Domain feature modules (co-locate components, hooks, types per domain)
├── services/        # Axios API call functions, one file per domain
├── hooks/           # Shared custom hooks
├── lib/             # axios.ts (pre-configured instance), utils.ts (cn, formatCurrency, formatDate)
├── providers/       # QueryProvider (TanStack Query), ThemeProvider (next-themes)
├── types/           # Shared TS types: ApiResponse<T>, PagedResponse<T>, ErrorResponse
└── constants/       # ROUTES, TOKEN_KEYS, PAGINATION_DEFAULTS
```

## Key Conventions

### Backend
- **Base package**: `com.corebanking`
- **API context path**: `/api` (set in `server.servlet.context-path`)
- **Error format**: All errors return `ErrorResponse` with `errorCode` (CBL-XXX prefix), `message`, `status`, `timestamp`, `path`
- **All entities** must extend `AuditableEntity` to get `createdAt`, `updatedAt`, `createdBy`, `updatedBy`
- **Profiles**: `dev` (create-drop JPA, DEBUG logs), `prod` (validate JPA, WARN logs), `test` (H2 in-memory)
- **MapStruct + Lombok**: The `lombok-mapstruct-binding` annotation processor order matters — already configured in `build.gradle.kts`
- **DTOs**: Use `@Valid` on controller method parameters; validation annotations live on the DTO, not the entity

### Frontend
- **HTTP client**: Import `apiClient` from `@/lib/axios` — never create raw axios instances
- **Currency**: Use `formatCurrency(amount)` from `@/lib/utils` (defaults to Peruvian Sol / PEN)
- **Styling**: Use `cn()` from `@/lib/utils` for conditional class merging (clsx + tailwind-merge)
- **Shadcn components**: Add with `npx shadcn@latest add <component>` — never write Radix boilerplate manually
- **Token storage**: Use `TOKEN_KEYS.ACCESS` and `TOKEN_KEYS.REFRESH` constants, stored in `localStorage`

## Environment Variables

Copy `.env.example` to `.env`. Key variables:
- `JWT_SECRET` — 64-char hex string, required (never commit the real value)
- `SPRING_PROFILES_ACTIVE` — `dev` | `prod` | `test`
- `NEXT_PUBLIC_API_URL` — backend base URL consumed by the frontend Axios client
