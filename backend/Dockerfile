# ─── Stage 1: Build ────────────────────────────────────────────────────────
FROM gradle:8.10-jdk21-alpine AS builder
WORKDIR /app

# Copy dependency manifests first for layer caching
COPY settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle

# Resolve dependencies (cached unless build files change)
RUN gradle dependencies --no-daemon --configuration compileClasspath 2>/dev/null || true

# Copy source and build the fat JAR (skip tests — CI runs them separately)
COPY src ./src
RUN gradle bootJar --no-daemon -x test

# ─── Stage 2: Runtime ──────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Non-root user for security hardening
RUN addgroup --system --gid 1001 appgroup \
 && adduser  --system --uid 1001 --ingroup appgroup appuser

COPY --from=builder /app/build/libs/*.jar app.jar

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/api/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
