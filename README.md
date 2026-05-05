# AssetVault

AssetVault is a Spring Boot backend for internal IT/Admin asset management. It tracks assets, employees, asset assignments, maintenance records, software licenses, dashboard metrics, and operational alerts.

This implementation keeps the project's existing structural direction, including the shared `Assignment` mapped superclass used by both asset and software assignments.

## Improvements Over The Base Requirements

This version intentionally differs from the assignment brief in a few places where the implementation is more robust, more observable, or easier to maintain:

| Base requirement | Current implementation delta | Why this is an improvement |
| --- | --- | --- |
| Spring Boot 3.3+ with SpringDoc OpenAPI 2.x | Uses Spring Boot 4.x with the SpringDoc 3.x MVC starter while keeping Swagger UI at `/swagger-ui.html`. | Keeps the project on the current Spring ecosystem while preserving the required API documentation route. |
| Auto-generate unique `assetCode` | Asset codes and optional employee codes are derived from database-generated IDs, with unique constraints as the final guard. | Avoids count-and-probe code generation races and produces stable codes like `LPT-00001` and `EMP-00001`. |
| `SoftwareLicense.assignedEmployees` as a direct `ManyToMany` | Software seats are tracked through `SoftwareAssignment`, which extends the shared `Assignment` mapped superclass and records status, dates, seat index, assignee, and remarks. | Preserves license assignment/revocation history instead of losing it in a simple join table. |
| License assign/revoke behavior inside the license module | Public endpoints remain `PATCH /api/v1/licenses/{id}/assign` and `/revoke`, but the business logic lives in `SoftwareAssignmentService`. | Separates license inventory from seat assignment workflows and makes each service easier to test. |
| Seat availability based on license `usedSeats` | Active software assignments are counted as the source of truth, license rows are locked during assignment changes, responses include `remainingSeats`, and exhausted-seat alerts focus on pooled non-`INDIVIDUAL` licenses. | Reduces concurrent over-allocation risk and makes seat alerts more actionable. |
| Basic asset/license lifecycle endpoints | Retiring or deleting an asset first returns any active asset assignment; deactivating or deleting a license revokes active software assignments before changing inventory state. | Prevents dangling active assignments and keeps inventory state consistent. |
| Employee deactivation triggers an asset recall check | Deactivation is blocked with `EmployeeHasActiveAssetsException` when the employee still has active asset assignments. | Preserves assignment history and forces assets to be returned before the employee is disabled. |
| Maintenance start/complete/cancel endpoints | Active maintenance drives asset status to `UNDER_MAINTENANCE`; complete/cancel/delete restores the asset to `ASSIGNED` or `AVAILABLE` based on current assignments and other active maintenance. | Keeps asset lifecycle state synchronized with maintenance records. |
| Maintenance status changes can be requested directly | Start requires a `SCHEDULED` record, completion requires an `IN_PROGRESS` record, and cancellation requires a `SCHEDULED` record; invalid transitions throw `MaintenanceStateException`. | Makes maintenance transitions explicit and centrally handled instead of requiring a full-record update. |
| Mark asset as lost | Marking a non-retired asset lost also cancels in-progress maintenance. | Handles real-world loss reporting without leaving active maintenance behind. |
| Bonus `updatedAt` tracking | JPA auditing is enabled and `updatedAt` is maintained on `Asset` and `SoftwareLicense`. | Removes manual timestamp bookkeeping. |
| Logging expectations | Dev-profile service logging is handled by an AOP aspect with method arguments, result summaries, timings, and exception messages; production keeps only normal INFO/WARN/ERROR logging. | Gives useful diagnostics during development without noisy production traces. |
| Swagger annotations and 80% controller/service coverage expectation | DTO schema annotations are enforced by `DtoSchemaDocumentationTest`, and JaCoCo gates controller and service implementation package line coverage at 80%. | Turns documentation and coverage expectations into build-checked guarantees. |
| Optional profiles, Actuator, and pagination/sorting | Dev H2 and prod PostgreSQL profiles are included, Actuator exposes `/actuator/health` and `/actuator/info`, and list-style endpoints accept `Pageable`. | Adds production-style operations and scalable API access beyond the minimum deliverable. |

## Tech Stack

| Area | Choice |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 4.x |
| Persistence | Spring Data JPA / Hibernate |
| Dev DB | H2 in-memory |
| Prod DB | PostgreSQL via Docker |
| API Docs | SpringDoc OpenAPI 3.x at `/swagger-ui.html` |
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
| Maintenance | `/api/v1/maintenance` | Create, list, asset/status/type filters, scheduled work, date range, update, start, complete, cancel, delete |
| Licenses | `/api/v1/licenses` | Create, list, expiring/expired, exhausted non-individual seats, search, update, assign/revoke seat through the software assignment service, deactivate, delete |
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
