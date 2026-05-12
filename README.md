# Core Banking Lite

Enterprise banking platform — Spring Boot 3 backend + Next.js 15 frontend.

## Stack

| Layer    | Technology                                               |
|----------|----------------------------------------------------------|
| Frontend | Next.js 15, React 19, TypeScript, TailwindCSS, Shadcn/ui |
| State    | TanStack Query v5, React Hook Form, Zod                  |
| HTTP     | Axios                                                    |
| Backend  | Java 21, Spring Boot 3, Spring Security                  |
| Database | PostgreSQL 16                                            |
| Auth     | JWT + Refresh Tokens (BCrypt)                            |
| Docs     | Swagger / OpenAPI 3                                      |
| Build    | Gradle 8 (Kotlin DSL)                                    |
| DevOps   | Docker, GitHub Actions, GCP                              |

## Modules

| Module    | Description                               |
|-----------|-------------------------------------------|
| Customers | Client management with KYC               |
| Accounts  | Savings, CTS, Current accounts           |
| Transfers | Inter-account transfers with idempotency  |
| Savings   | Savings products management              |
| Audit     | Financial audit trail                    |
| Dashboard | Financial KPIs and analytics             |

## Project Structure

```
core-banking-lite/
├── backend/           # Spring Boot 3 + Java 21
├── frontend/          # Next.js 15 + React 19
├── scripts/           # DB init scripts
├── docker-compose.yml
└── .env.example
```

## Quick Start

### Prerequisites
- Java 21
- Node.js 20+
- Docker & Docker Compose
- Gradle 8.7+ (to generate wrapper scripts)

### 1. Environment setup
```bash
cp .env.example .env
```

### 2. Start database
```bash
docker compose up postgres -d
```

### 3. Backend
```bash
cd backend
gradle wrapper --gradle-version 8.7   # First time only
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 4. Frontend
```bash
cd frontend
npm install
npm run dev
```

## API Documentation
- Swagger UI: `http://localhost:8080/api/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api/v3/api-docs`

## Full stack with Docker
```bash
docker compose --profile full up -d
```
