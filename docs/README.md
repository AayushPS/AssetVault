# AssetVault Documentation

This directory holds the maintainers' view of the project. The top-level [README](../README.md) remains the public-facing project overview and API inventory, while the files here explain how the system is structured, operated, and tested.

## Documentation Map

| Topic | File | When to use it |
|-------|------|----------------|
| System design and business rules | [architecture.md](architecture.md) | Understand package responsibilities, entity relationships, status models, and lifecycle rules |
| Runtime behavior and deployment | [operations.md](operations.md) | Run the application locally, switch profiles, start PostgreSQL, and understand operational constraints |
| Test strategy and execution | [testing.md](testing.md) | Run the suite, inspect reports, and understand coverage gates and test layers |
| Full route inventory | [../README.md#api-reference](../README.md#api-reference) | Browse the endpoint catalog by module |
| Generated quality plan | [../TEST_PLAN.md](../TEST_PLAN.md) | Review the larger testing backlog and gap analysis |
| Fast developer commands | [../HELP.md](../HELP.md) | Use the short command reference during day-to-day work |
| Product/source brief | [../AssetVault1-1.md](../AssetVault1-1.md) | Trace the implementation back to the original project brief |

## Current System Snapshot

| Area | Value |
|------|-------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.6 |
| Build tool | Maven Wrapper (`mvnw`, `mvnw.cmd`) |
| API style | Spring MVC REST API under `/api/v1` |
| Profiles | `dev` by default, `prod` for PostgreSQL |
| Dev database | H2 in-memory (`create-drop`) |
| Prod database | PostgreSQL (`update` by default) |
| API documentation | SpringDoc Swagger UI at `/swagger-ui.html` |
| Operational endpoints | Actuator `health` and `info` |
| Security posture | No Spring Security starter is currently configured in the application runtime |

## How To Read This Repo

1. Start with [../README.md](../README.md) for scope, features, and the endpoint list.
2. Use [architecture.md](architecture.md) to understand domain rules before changing business logic.
3. Use [operations.md](operations.md) before running locally, switching environments, or deploying.
4. Use [testing.md](testing.md) before changing behavior or debugging regressions.

## Source-of-Truth Notes

- Profile behavior is defined in `src/main/resources/application*.properties`.
- Endpoint paths are defined in the controller classes under `src/main/java/com/assetvault/controller`.
- Error response behavior is defined centrally in `GlobalExceptionHandler`.
- Coverage gates are defined in `pom.xml` via the JaCoCo Maven plugin.
