# AssetVault

> Internal IT Asset Management REST API built with Java 21 and Spring Boot 4.x

AssetVault is a production-ready backend system for company IT/Admin departments to manage the complete lifecycle of internal assets — from procurement and assignment to maintenance, transfers, and retirement.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Quick Start](#quick-start)
- [API Reference](#api-reference)
- [Configuration](#configuration)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Design Decisions](#design-decisions)
- [Contributing](#contributing)
- [License](#license)

---

## Features

- **Asset Lifecycle Management** — Register, assign, maintain, retire, and track assets across their full lifecycle
- **Employee Management** — Track employees with department/designation, auto-generated codes, and active/inactive status
- **Smart Assignments** — Assign/return/transfer assets with full history tracking and conflict prevention
- **Maintenance Scheduling** — Schedule, start, complete, or cancel maintenance with automatic asset status sync
- **Software License Tracking** — Pool-based seat management with assign/revoke workflows and expiry alerts
- **Dashboard & Analytics** — Real-time metrics including type breakdown, department summary, warranty alerts, and asset value
- **Pagination & Sorting** — All list endpoints support Spring `Pageable` for scalable data access
- **OpenAPI Documentation** — Auto-generated Swagger UI at `/swagger-ui.html`
- **Profile-based Configuration** — Development (H2) and Production (PostgreSQL) profiles
- **80%+ Code Coverage** — JaCoCo-enforced coverage gate on controller and service layers

---

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 21 (LTS) |
| Framework | Spring Boot 4.0.6 |
| Persistence | Spring Data JPA / Hibernate |
| Dev Database | H2 In-Memory |
| Prod Database | PostgreSQL (Docker) |
| API Documentation | SpringDoc OpenAPI 3.x |
| Validation | Jakarta Bean Validation |
| Build Tool | Maven |
| Testing | JUnit 5, Mockito, MockMvc, @DataJpaTest |
| Coverage | JaCoCo (80% gate) |
| Logging | SLF4J + Logback |
| Monitoring | Spring Actuator |

---

## Architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│                           REST Clients                                │
└───────────────────────────────┬──────────────────────────────────────┘
                                │ HTTP
┌───────────────────────────────▼──────────────────────────────────────┐
│  Controller Layer (6 controllers, 70+ endpoints)                     │
│  - Request validation (Jakarta Bean Validation)                      │
│  - DTO serialization (Jackson)                                       │
│  - Exception mapping (GlobalExceptionHandler → ErrorResponse)        │
└───────────────────────────────┬──────────────────────────────────────┘
                                │
┌───────────────────────────────▼──────────────────────────────────────┐
│  Service Layer (7 service interfaces + implementations)              │
│  - Business logic & validation                                       │
│  - Cross-entity operations (assignment workflows)                    │
│  - Transaction management (@Transactional)                           │
└───────────────────────────────┬──────────────────────────────────────┘
                                │
┌───────────────────────────────▼──────────────────────────────────────┐
│  Repository Layer (6 Spring Data JPA repositories)                   │
│  - Custom queries (JPQL, derived methods)                            │
│  - Pagination support                                                │
└───────────────────────────────┬──────────────────────────────────────┘
                                │
┌───────────────────────────────▼──────────────────────────────────────┐
│  Database (H2 dev / PostgreSQL prod)                                 │
└──────────────────────────────────────────────────────────────────────┘
```

### Entity Relationship Diagram

```
┌─────────────┐         ┌──────────────────┐         ┌────────────┐
│   Employee  │◄────────│  AssetAssignment │────────►│   Asset    │
│             │         │  (extends        │         │            │
│  id         │         │   Assignment)    │         │  id        │
│  name       │         │                  │         │  assetCode │
│  email      │         │  assignedDate    │         │  name      │
│  department │         │  returnedDate    │         │  type      │
│  employeeCode│        │  status          │         │  status    │
│  isActive   │         │  assignedBy      │         │  serialNo  │
└──────┬──────┘         └──────────────────┘         └─────┬──────┘
       │                                                    │
       │         ┌───────────────────────┐                  │
       │◄────────│  SoftwareAssignment   │         ┌───────▼────────┐
       │         │  (extends Assignment) │         │MaintenanceRecord│
       │         │                       │         │                │
       │         │  seatIndex            │         │  type          │
       │         └───────────┬───────────┘         │  status        │
       │                     │                     │  scheduledDate │
       │         ┌───────────▼───────────┐         │  completedDate │
       │         │  SoftwareLicense      │         └────────────────┘
       │         │                       │
       │         │  softwareName         │
       │         │  licenseKey           │
       │         │  totalSeats           │
       │         │  usedSeats            │
       │         └───────────────────────┘
```

---

## Quick Start

### Prerequisites

- Java 21+
- Maven 3.9+ (or use the included `mvnw` wrapper)

### Run in Development Mode (H2)

```bash
./mvnw spring-boot:run
```

On Windows:
```cmd
mvnw.cmd spring-boot:run
```

### Useful URLs (Dev)

| Resource | URL |
|----------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| H2 Console | http://localhost:8080/h2-console |
| Health Check | http://localhost:8080/actuator/health |
| App Info | http://localhost:8080/actuator/info |

### H2 Console Credentials

| Field | Value |
|-------|-------|
| JDBC URL | `jdbc:h2:mem:assetvaultdb` |
| User | `sa` |
| Password | *(leave blank)* |

### Run in Production Mode (PostgreSQL)

Start the database:
```bash
docker compose --env-file src/main/resources/docker/.env \
  -f src/main/resources/docker/docker-compose.yml up -d
```

Run the application:
```bash
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
```

Stop the database:
```bash
docker compose --env-file src/main/resources/docker/.env \
  -f src/main/resources/docker/docker-compose.yml down
```

---

## API Reference

All endpoints are under the base path `/api/v1`. Responses use DTOs — entities are never exposed directly.

### Asset Module — `/api/v1/assets`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Register a new asset |
| GET | `/` | Get all assets (paginated + sorted) |
| GET | `/{id}` | Get asset by ID |
| GET | `/code/{assetCode}` | Get asset by unique code |
| GET | `/type/{type}` | Filter by type (LAPTOP, DESKTOP, etc.) |
| GET | `/status/{status}` | Filter by status (AVAILABLE, ASSIGNED, etc.) |
| GET | `/available` | Get all available assets |
| GET | `/department?name=` | Get assets assigned in a department |
| GET | `/warranty-expiring?days=` | Warranty expiring within N days |
| GET | `/warranty-expired` | All expired warranties |
| GET | `/search?keyword=` | Search by name, brand, or model |
| GET | `/low-value?maxCost=` | Assets below a purchase cost |
| PUT | `/{id}` | Update asset details |
| PATCH | `/{id}/status?status=` | Update asset status |
| PATCH | `/{id}/retire` | Retire an asset (releases assignments) |
| PATCH | `/{id}/mark-lost` | Mark as lost (cancels maintenance) |
| DELETE | `/{id}` | Delete asset record |

### Employee Module — `/api/v1/employees`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Register a new employee |
| GET | `/` | Get all employees (paginated) |
| GET | `/{id}` | Get employee by ID |
| GET | `/search?name=` | Search by name |
| GET | `/department?name=` | Filter by department |
| GET | `/{id}/assets` | Get assigned assets |
| GET | `/{id}/licenses` | Get assigned software licenses |
| GET | `/{id}/assignment-history` | Full assignment history |
| PUT | `/{id}` | Update employee details |
| PATCH | `/{id}/deactivate` | Deactivate (blocks if active assets) |
| DELETE | `/{id}` | Delete employee record |

### Assignment Module — `/api/v1/assignments`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Assign an asset to an employee |
| GET | `/` | Get all assignments (paginated) |
| GET | `/{id}` | Get assignment by ID |
| GET | `/active` | Get active assignments |
| GET | `/asset/{assetId}` | Assignment history for an asset |
| GET | `/employee/{employeeId}` | Assignment history for an employee |
| GET | `/date-range?from=&to=` | Assignments within date range |
| PATCH | `/{id}/return` | Return an asset |
| PATCH | `/{id}/transfer?toEmployeeId=` | Transfer to another employee |
| DELETE | `/{id}` | Delete assignment record |

### Maintenance Module — `/api/v1/maintenance`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Create maintenance record |
| GET | `/` | Get all records (paginated) |
| GET | `/{id}` | Get record by ID |
| GET | `/asset/{assetId}` | Records for an asset |
| GET | `/status/{status}` | Filter by status |
| GET | `/type/{type}` | Filter by type |
| GET | `/scheduled` | Upcoming scheduled maintenance |
| GET | `/date-range?from=&to=` | Records within date range |
| PUT | `/{id}` | Update maintenance record |
| PATCH | `/{id}/start` | Start maintenance (SCHEDULED → IN_PROGRESS) |
| PATCH | `/{id}/complete` | Complete maintenance (IN_PROGRESS → COMPLETED) |
| PATCH | `/{id}/cancel` | Cancel maintenance (SCHEDULED → CANCELLED) |
| DELETE | `/{id}` | Delete maintenance record |

### License Module — `/api/v1/licenses`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Add software license |
| GET | `/` | Get all licenses (paginated) |
| GET | `/{id}` | Get license by ID |
| GET | `/expiring-soon?days=` | Expiring within N days |
| GET | `/expired` | All expired licenses |
| GET | `/low-seats` | No remaining seats (non-INDIVIDUAL) |
| GET | `/search?name=` | Search by software name |
| PUT | `/{id}` | Update license details |
| PATCH | `/{id}/assign?employeeId=` | Assign a seat to employee |
| PATCH | `/{id}/revoke?employeeId=` | Revoke seat from employee |
| PATCH | `/{id}/deactivate` | Deactivate (revokes all seats) |
| DELETE | `/{id}` | Delete license record |

### Dashboard Module — `/api/v1/dashboard`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/summary` | Total/assigned/available/maintenance/retired counts |
| GET | `/type-breakdown` | Asset count grouped by type |
| GET | `/department-assets` | Asset count and value per department |
| GET | `/warranty-alerts` | Warranty expiring within 30 days |
| GET | `/license-alerts` | Expiring licenses + exhausted seats |
| GET | `/maintenance-summary` | Scheduled/in-progress/completed counts |
| GET | `/asset-value` | Total purchase cost of active assets |

### Error Response Format

All errors return a consistent structure:
```json
{
  "timestamp": "2026-05-07T09:45:00",
  "status": 409,
  "error": "Conflict",
  "message": "Asset LPT-00123 is currently ASSIGNED and cannot be reassigned",
  "path": "/api/v1/assignments",
  "fieldErrors": null
}
```

### HTTP Status Codes

| Status | Condition |
|--------|-----------|
| 200 | Successful retrieval or update |
| 201 | Successful creation |
| 204 | Successful deletion |
| 400 | Validation failure (field errors included) |
| 404 | Resource not found |
| 409 | Business rule conflict |
| 422 | Semantic error (e.g., expired license) |
| 500 | Unexpected server error |

---

## Configuration

### Profiles

| Profile | Database | Logging | Use Case |
|---------|----------|---------|----------|
| `dev` (default) | H2 in-memory | DEBUG | Local development |
| `prod` | PostgreSQL | INFO | Production deployment |

### Environment Variables (Production)

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/assetvaultdb` | Database JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `assetvault_user` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `assetvault_pass` | Database password |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` | DDL strategy |

### Key Configuration Properties

```properties
# Base
spring.application.name=AssetVault
spring.profiles.default=dev
spring.jackson.default-property-inclusion=non_null
spring.jpa.open-in-view=false

# Actuator
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always

# API Docs
springdoc.swagger-ui.path=/swagger-ui.html
```

---

## Testing

### Run All Tests

```bash
./mvnw test
```

### Run Tests Without Coverage Enforcement

```bash
./mvnw -Djacoco.skip=true test
```

On Windows PowerShell:
```powershell
.\mvnw.cmd --% -Djacoco.skip=true test
```

### Run a Specific Test Class

```powershell
.\mvnw.cmd --% -Djacoco.skip=true test -Dtest=AssetControllerTest
```

### Test Coverage Report

After running tests, the JaCoCo report is at:
```
target/site/jacoco/index.html
```

### Test Architecture

| Layer | Approach | Annotations |
|-------|----------|-------------|
| Controller | MockMvc HTTP contract tests | `@WebMvcTest` |
| Service | Unit tests with mocked repositories | `@ExtendWith(MockitoExtension.class)` |
| Repository | Integration tests with H2 | `@DataJpaTest` |
| DTO | Validation constraint tests | `@SpringBootTest` (minimal) |
| Exception | Error response format tests | `@WebMvcTest` |
| E2E | Full stack with embedded H2 | `@SpringBootTest` + MockMvc |

### Test Summary

- **293 tests** across all layers
- **80%+ line coverage** on controller and service packages (JaCoCo-enforced)
- Covers: CRUD operations, business rule validation, error handling, pagination, search, lifecycle state transitions

### Coverage Gates

JaCoCo enforces 80% line coverage on:
- `com.assetvault.controller`
- `com.assetvault.service.impl`

---

## Project Structure

```
src/main/java/com/assetvault/
├── AssetVaultApplication.java          # Application entry point
├── config/
│   ├── JpaAuditingConfig.java          # Enables @CreatedDate / @LastModifiedDate
│   ├── ServiceLoggingAspect.java       # AOP logging for service methods (dev profile)
│   └── SwaggerConfig.java             # OpenAPI metadata configuration
├── controller/
│   ├── AssetController.java           # Asset CRUD + lifecycle endpoints
│   ├── AssignmentController.java      # Asset assignment/return/transfer
│   ├── DashboardController.java       # Analytics and alerts
│   ├── EmployeeController.java        # Employee CRUD + deactivation
│   ├── LicenseController.java         # Software license management
│   └── MaintenanceController.java     # Maintenance scheduling/tracking
├── dto/                                # Request/Response DTOs (18 classes)
├── exception/
│   ├── GlobalExceptionHandler.java    # Centralized @RestControllerAdvice
│   └── [18 custom exception classes]  # Typed business rule violations
├── model/
│   ├── Asset.java                     # Core asset entity
│   ├── Employee.java                  # Employee entity
│   ├── Assignment.java               # @MappedSuperclass for assignments
│   ├── AssetAssignment.java          # Asset ↔ Employee assignment
│   ├── SoftwareAssignment.java       # License ↔ Employee seat assignment
│   ├── SoftwareLicense.java          # Software license entity
│   ├── MaintenanceRecord.java        # Maintenance history entity
│   └── enums/                         # AssetType, AssetStatus, etc. (6 enums)
├── repository/                        # 6 Spring Data JPA repositories
├── service/
│   ├── [7 service interfaces]
│   └── impl/                          # 7 service implementations
└── util/
    ├── CodeGenerator.java             # Asset/Employee code generation
    └── Mapper.java                    # Entity → DTO mapping
```

---

## Design Decisions

### Shared Assignment Superclass

Both `AssetAssignment` and `SoftwareAssignment` extend the `Assignment` `@MappedSuperclass`, sharing common fields (employee, dates, status, assignedBy, remarks). This avoids duplication while keeping each in its own table.

### Code Generation Strategy

Asset codes (e.g., `LPT-00001`) and employee codes (e.g., `EMP-00001`) are derived from database-generated IDs after the initial save, then updated. A unique constraint acts as the final guard against races.

### Software License Seat Tracking

Rather than a simple `@ManyToMany` between licenses and employees, seats are tracked through `SoftwareAssignment` records. This preserves assignment/revocation history, timestamps, and the person responsible.

### Pessimistic Locking on License Assignment

License rows are locked (`@Lock(PESSIMISTIC_WRITE)`) during assign/revoke operations to prevent concurrent over-allocation of seats.

### Cascading Lifecycle Operations

- **Retire/Delete Asset** → Releases active assignments first
- **Deactivate/Delete License** → Revokes active software assignments first
- **Mark Asset Lost** → Cancels in-progress maintenance
- **Deactivate Employee** → Blocked if active asset assignments exist

### Maintenance State Machine

Transitions are explicitly validated:
- `start`: Requires SCHEDULED status
- `complete`: Requires IN_PROGRESS status
- `cancel`: Requires SCHEDULED status

Invalid transitions throw `MaintenanceStateException`.

### AOP Service Logging (Dev Only)

A Spring AOP aspect logs method entry/exit with arguments, results, timing, and exceptions for all service methods — but only in the `dev` profile to keep production logs clean.

---

## Improvements Over Base Requirements

| Base Requirement | Implementation | Rationale |
|------------------|---------------|-----------|
| Spring Boot 3.3+ / SpringDoc 2.x | Spring Boot 4.x / SpringDoc 3.x | Current ecosystem, same Swagger UI route |
| Auto-generated asset codes | ID-based generation with unique constraints | Avoids race conditions |
| `SoftwareLicense.assignedEmployees` as `@ManyToMany` | `SoftwareAssignment` entity with full history | Preserves assignment/revocation audit trail |
| License assign/revoke in license module | Delegated to `SoftwareAssignmentService` | Separation of concerns |
| Basic seat count | Active assignments counted as source of truth | Prevents over-allocation |
| Employee deactivation | Blocked with exception if active assets exist | Preserves assignment history integrity |
| Maintenance start/complete/cancel | Explicit state machine transitions | Invalid transitions throw `MaintenanceStateException` |
| Mark asset as lost | Also cancels in-progress maintenance | Real-world loss handling |
| JPA auditing for timestamps | `@CreatedDate` / `@LastModifiedDate` | Automatic, no manual bookkeeping |

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Write tests first (TDD encouraged)
4. Ensure all tests pass: `./mvnw test`
5. Verify JaCoCo coverage gates are met
6. Commit with descriptive messages
7. Push and open a Pull Request

### Code Style

- Follow standard Java conventions (PascalCase classes, camelCase methods)
- Use Lombok for boilerplate reduction
- All public API changes require corresponding DTO updates
- Never expose JPA entities directly in responses
- Add `@Schema` annotations to DTOs for OpenAPI documentation

---

## License

This project is licensed under the MIT License.
