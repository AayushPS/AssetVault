# AssetVault

AssetVault is a Spring Boot backend for internal IT/Admin asset management. It tracks assets, employees, asset assignments, maintenance records, software licenses, dashboard metrics, and operational alerts.

This implementation keeps the project's existing structural direction, including the shared `Assignment` mapped superclass used by both asset and software assignments.

## Tech Stack

| Area | Choice |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 4.x |
| Persistence | Spring Data JPA / Hibernate |
| Dev DB | H2 in-memory |
| Prod DB | PostgreSQL via Docker |
| API Docs | SpringDoc OpenAPI at `/swagger-ui.html` |
| Testing | JUnit 5, Mockito, MockMvc, `@DataJpaTest` |
| Observability | Actuator `/actuator/health` and `/actuator/info`, SLF4J + Logback |

## Quick Start: Dev Profile

```bash
bash mvnw spring-boot:run
```

Useful URLs:

| Tool | URL |
| --- | --- |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| H2 Console | http://localhost:8080/h2-console |
| Health | http://localhost:8080/actuator/health |

H2 console settings:

| Field | Value |
| --- | --- |
| JDBC URL | `jdbc:h2:mem:assetvaultdb` |
| User | `sa` |
| Password | leave blank |

## Production Profile With Docker Postgres

Start PostgreSQL:

```bash
docker compose --env-file src/main/resources/docker/.env -f src/main/resources/docker/docker-compose.yml up -d
```

Run the API against PostgreSQL:

```bash
SPRING_PROFILES_ACTIVE=prod bash mvnw spring-boot:run
```

Default prod database values come from `src/main/resources/docker/.env` and can be overridden with:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/assetvaultdb
SPRING_DATASOURCE_USERNAME=assetvault_user
SPRING_DATASOURCE_PASSWORD=assetvault_pass
```

Stop PostgreSQL:

```bash
docker compose --env-file src/main/resources/docker/.env -f src/main/resources/docker/docker-compose.yml down
```

## API Overview

| Module | Base Path | Highlights |
| --- | --- | --- |
| Assets | `/api/v1/assets` | Create, list, find by code/type/status, available assets, department assets, warranty alerts, search, low-value filter, status updates, retire/lost/delete |
| Employees | `/api/v1/employees` | Create, list, search, department filter, assigned assets/licenses, assignment history, update, deactivate, delete |
| Assignments | `/api/v1/assignments` | Assign, list, active assignments, asset/employee history, date range, return, transfer, delete |
| Maintenance | `/api/v1/maintenance` | Create, list, asset/status/type filters, scheduled work, date range, update, complete, cancel, delete |
| Licenses | `/api/v1/licenses` | Create, list, expiring/expired, exhausted seats, search, update, assign/revoke seat, deactivate, delete |
| Dashboard | `/api/v1/dashboard` | Summary, type breakdown, department assets, warranty alerts, license alerts, maintenance summary, asset value |

All API responses use DTOs rather than exposing JPA entities directly.

## Tests

Run all tests:

```bash
bash mvnw test
```

The suite includes controller validation/error tests, service business-rule tests, a Spring context test, and repository query tests.

## Assignment Reference

The original PDF has been converted for quick reference at:

```text
AssetVault1-1.md
```
