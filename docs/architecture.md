# AssetVault Architecture Guide

## Purpose

AssetVault is a layered Spring MVC application for managing internal company assets and related operational records. The codebase is optimized for predictable REST contracts, explicit business-rule enforcement, and a separation between transport DTOs, service logic, and persistence.

## Request Flow

1. A request enters one of the REST controllers under `com.assetvault.controller`.
2. Spring validation rejects malformed request bodies or invalid parameters before business logic runs.
3. Service implementations under `com.assetvault.service.impl` execute business rules and transaction-scoped operations.
4. Spring Data JPA repositories query and persist the domain model.
5. `GlobalExceptionHandler` converts validation failures and domain exceptions into a consistent JSON error contract.

## Layer Responsibilities

| Layer | Package | Responsibility |
|-------|---------|----------------|
| Web/API | `com.assetvault.controller` | HTTP routing, request validation, pageable/sortable endpoints, response status codes |
| DTOs | `com.assetvault.dto` | Request/response contracts, OpenAPI schema metadata, error payloads |
| Service interfaces | `com.assetvault.service` | Public business capabilities consumed by controllers |
| Service implementations | `com.assetvault.service.impl` | Transactional business rules, state transitions, cross-entity coordination |
| Persistence | `com.assetvault.repository` | Repository queries, pagination, lock-based seat allocation support |
| Domain model | `com.assetvault.model` | JPA entities and shared assignment abstractions |
| Cross-cutting config | `com.assetvault.config` | Auditing, Swagger/OpenAPI metadata, service logging aspect |
| Utilities | `com.assetvault.util` | Code generation and entity-to-DTO mapping helpers |

## Domain Model

| Entity | Role in the system | Key relationships |
|--------|--------------------|-------------------|
| `Asset` | Physical item tracked by IT, such as a laptop or monitor | Can have many `AssetAssignment` records and many `MaintenanceRecord` entries over time |
| `Employee` | Person who can hold assets or software seats | Can have many asset assignments and software assignments |
| `Assignment` | Shared mapped superclass for assignment audit fields | Parent abstraction for asset and software assignment records |
| `AssetAssignment` | Historical record of an asset being assigned, returned, or transferred | Connects one `Asset` with one `Employee` at a point in time |
| `SoftwareLicense` | Trackable software entitlement with seat counts and expiry | Can have many `SoftwareAssignment` records |
| `SoftwareAssignment` | Historical record of a license seat being granted or revoked | Connects one `SoftwareLicense` with one `Employee` |
| `MaintenanceRecord` | Record of planned or completed maintenance work on an asset | Belongs to one `Asset` |

## State Catalogs

### Asset status

`AVAILABLE`, `ASSIGNED`, `UNDER_MAINTENANCE`, `RETIRED`, `LOST`

### Assignment status

`ACTIVE`, `RETURNED`, `TRANSFERRED`

### Maintenance status

`SCHEDULED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`

### Asset types

`LAPTOP`, `DESKTOP`, `MONITOR`, `KEYBOARD`, `MOUSE`, `HEADSET`, `PHONE`, `PRINTER`, `OTHER`

### License types

`INDIVIDUAL`, `FLOATING`, `SITE`

### Maintenance types

`REPAIR`, `SERVICE`, `UPGRADE`, `INSPECTION`

## Core Business Rules

1. Assets cannot be actively assigned unless they are in an assignable state.
2. Retiring or deleting an asset cascades through active assignment cleanup before final state changes.
3. Marking an asset as lost also cancels in-progress maintenance activity tied to that asset.
4. Employees cannot be deactivated while they still hold active asset assignments.
5. Software seat allocation is modeled as a first-class entity (`SoftwareAssignment`) rather than a direct many-to-many relationship, preserving history and accountability.
6. License assignment and revocation use lock-aware persistence to avoid over-allocating seats during concurrent requests.
7. Maintenance transitions are explicit: only `SCHEDULED -> IN_PROGRESS`, `IN_PROGRESS -> COMPLETED`, and `SCHEDULED -> CANCELLED` are valid.
8. Exceptions are business-significant. The codebase uses typed exceptions for conflicts, not-found cases, validation errors, and semantic failures.

## Error Handling Contract

Every controller shares the same error payload shape through `GlobalExceptionHandler`:

- `timestamp`
- `status`
- `error`
- `message`
- `path`
- `fieldErrors` for validation-oriented failures

This keeps HTTP-contract testing stable across modules and makes downstream API consumption more predictable.

## Cross-Cutting Behaviors

| Concern | Current implementation |
|---------|------------------------|
| Validation | Jakarta Bean Validation on request DTOs and request parameters |
| Transactions | Service-layer orchestration with transactional state changes |
| Auditing | JPA auditing configuration for created/updated timestamps |
| Logging | Service logging aspect enabled for development-focused tracing |
| Serialization | Jackson omits `null` properties by default |
| Persistence guardrails | `spring.jpa.open-in-view=false` keeps lazy loading out of the web layer |

## Package Map

```text
src/main/java/com/assetvault
|- config/
|- controller/
|- dto/
|- exception/
|- model/
|  `- enums/
|- repository/
|- service/
|  `- impl/
`- util/
```

## Architecture Notes For Contributors

- Keep controllers thin. New business decisions belong in services, not in request handlers.
- Extend the DTO layer instead of exposing JPA entities directly.
- Add or update dedicated exception types when adding a new business-rule conflict.
- Keep state transitions explicit and testable. Do not hide them behind loosely defined status updates.
- Preserve the current layered package structure unless there is a clear architectural reason to change it.
