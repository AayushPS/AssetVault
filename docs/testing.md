# AssetVault Testing Guide

## Current Test Snapshot

The current repository state includes:

- 20 Java test source files under `src/test/java`
- 293 executed tests in the latest Surefire report set
- 0 failures, 0 errors, and 0 skipped tests in the latest recorded run
- JaCoCo coverage enforcement on controllers and service implementations

This snapshot is based on the reports currently present in `target/surefire-reports`.

## Test Layers

| Layer | Main goal | Typical tooling | Representative classes |
|------|-----------|-----------------|------------------------|
| Controller slice tests | Verify HTTP contracts, validation, and mapped responses | `@WebMvcTest`, MockMvc, Mockito | `AssetControllerTest`, `EmployeeControllerTest`, `LicenseControllerTest`, `RequestValidationTest` |
| Service unit tests | Verify business rules and state transitions in isolation | JUnit 5, Mockito, AssertJ | `AssetServiceImplTest`, `SoftwareLicenseServiceImplTest`, `MaintenanceServiceImplTest` |
| Repository integration tests | Verify JPA queries against H2 | `@DataJpaTest`, TestEntityManager | `AssetRepositoryTest`, `RepositoryQueryCoverageTest` |
| Cross-cutting tests | Verify DTO schema metadata, exception contracts, and aspect behavior | Spring test support + focused assertions | `DtoSchemaDocumentationTest`, `GlobalExceptionHandlerTest`, `ServiceLoggingAspectDevTest`, `ServiceLoggingAspectProdTest` |
| Application smoke tests | Verify that the Spring context loads and critical wiring is intact | `@SpringBootTest` | `AssetVaultApplicationTests` |

## Standard Commands

### Run the full suite

```bash
./mvnw test
```

On Windows CMD:

```cmd
mvnw.cmd test
```

### Run the suite without JaCoCo gate enforcement

```bash
./mvnw -Djacoco.skip=true test
```

On PowerShell, pass dotted Maven properties through `--%`:

```powershell
.\mvnw.cmd --% -Djacoco.skip=true test
```

### Run a single test class

```powershell
.\mvnw.cmd --% -Djacoco.skip=true test -Dtest=AssetControllerTest
```

## Coverage Enforcement

JaCoCo is configured in `pom.xml` to enforce a minimum line coverage ratio of `0.80` for:

- `com.assetvault.controller`
- `com.assetvault.service.impl`

This is a quality gate, not just a report. A test run can fail even when the code compiles if those package ratios regress below the configured threshold.

## Report Locations

| Path | Contents |
|------|----------|
| `target/surefire-reports/` | Per-test XML and text reports for the latest Maven Surefire run |
| `target/site/jacoco/index.html` | Human-readable coverage report |
| `target/jacoco.exec` | Raw JaCoCo execution data |

## When To Use Which Test Style

1. Use controller slice tests for request validation, endpoint status codes, paging parameters, and error payload shape.
2. Use service unit tests when adding or changing business rules, state transitions, and conflict handling.
3. Use repository tests when introducing custom queries or changing persistence behavior.
4. Use broader Spring tests sparingly, mainly for application wiring and cross-layer smoke coverage.

## Working Relationship With The Test Plan

`TEST_PLAN.md` is the broader gap-analysis and coverage roadmap.

- Use this guide for day-to-day execution and interpretation.
- Use `TEST_PLAN.md` when deciding what test areas still need to be added or expanded.

## Practical Contributor Checklist

1. Add or update the narrowest possible test first.
2. Run the smallest relevant slice locally before running the full suite.
3. Run the full suite before pushing changes that affect business logic or controller contracts.
4. Inspect JaCoCo output whenever controller or service implementation coverage drops.
