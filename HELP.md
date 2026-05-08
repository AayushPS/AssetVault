# AssetVault Developer Help

This file is the short-form command reference for working on the repository. For full project documentation, use [README.md](README.md) and [docs/README.md](docs/README.md).

## Common Commands

### Run the application in development mode

```bash
./mvnw spring-boot:run
```

On Windows CMD:

```cmd
mvnw.cmd spring-boot:run
```

### Run all tests

```bash
./mvnw test
```

### Skip JaCoCo gate enforcement during targeted test work

```powershell
.\mvnw.cmd --% -Djacoco.skip=true test
```

### Run one test class in PowerShell

```powershell
.\mvnw.cmd --% -Djacoco.skip=true test -Dtest=AssetControllerTest
```

### Start PostgreSQL for the `prod` profile

```bash
docker compose --env-file src/main/resources/docker/.env -f src/main/resources/docker/docker-compose.yml up -d
```

### Run with the `prod` profile

```bash
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
```

## Local URLs

| Surface | URL |
|---------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| H2 console | http://localhost:8080/h2-console |
| Actuator health | http://localhost:8080/actuator/health |

## Where To Look Next

| Need | File |
|------|------|
| Project overview and API inventory | [README.md](README.md) |
| Architecture and domain rules | [docs/architecture.md](docs/architecture.md) |
| Runtime and deployment notes | [docs/operations.md](docs/operations.md) |
| Test strategy and report locations | [docs/testing.md](docs/testing.md) |
| Test expansion roadmap | [TEST_PLAN.md](TEST_PLAN.md) |

## Important Current Constraints

1. `dev` is the default profile and uses an in-memory H2 database.
2. The application currently has no built-in Spring Security layer.
3. Actuator only exposes `health` and `info` over HTTP.


