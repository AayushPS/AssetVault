# AssetVault Operations Guide

## Runtime Profiles

| Profile | Properties file | Database | DDL strategy | Logging |
|---------|-----------------|----------|--------------|---------|
| `dev` | `src/main/resources/application-dev.properties` | H2 in-memory | `create-drop` | `com.assetvault=DEBUG` |
| `prod` | `src/main/resources/application-prod.properties` | PostgreSQL | `update` by default | `com.assetvault=INFO` |

The application defaults to the `dev` profile through `spring.profiles.default=dev`.

## Local Development Workflow

### Start with the default development profile

```bash
./mvnw spring-boot:run
```

On Windows CMD:

```cmd
mvnw.cmd spring-boot:run
```

### Useful local URLs

| Surface | URL |
|---------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| H2 console | http://localhost:8080/h2-console |
| Health | http://localhost:8080/actuator/health |
| Info | http://localhost:8080/actuator/info |

### H2 console settings

| Field | Value |
|-------|-------|
| JDBC URL | `jdbc:h2:mem:assetvaultdb` |
| User | `sa` |
| Password | blank |

## Production-Like PostgreSQL Workflow

The repository ships a Docker Compose definition at `src/main/resources/docker/docker-compose.yml` for local PostgreSQL startup.

### Start PostgreSQL

```bash
docker compose --env-file src/main/resources/docker/.env -f src/main/resources/docker/docker-compose.yml up -d
```

### Run the application in `prod`

```bash
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
```

### Stop PostgreSQL

```bash
docker compose --env-file src/main/resources/docker/.env -f src/main/resources/docker/docker-compose.yml down
```

## Production Environment Variables

| Variable | Default | Meaning |
|----------|---------|---------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/assetvaultdb` | JDBC URL for PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | `assetvault_user` | Database user |
| `SPRING_DATASOURCE_PASSWORD` | `assetvault_pass` | Database password |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` | Hibernate schema management strategy |

## API Surface Summary

The application exposes all business endpoints below the `/api/v1` base path.

| Module | Base path | Purpose |
|--------|-----------|---------|
| Assets | `/api/v1/assets` | Asset registration, search, status changes, retirement, loss handling |
| Employees | `/api/v1/employees` | Employee registration, search, deactivation, assignment history lookup |
| Assignments | `/api/v1/assignments` | Asset assignment, return, transfer, and history queries |
| Maintenance | `/api/v1/maintenance` | Maintenance scheduling and state progression |
| Licenses | `/api/v1/licenses` | Software license CRUD, seat assignment, expiry/seat alerts |
| Dashboard | `/api/v1/dashboard` | Aggregated counts, alert summaries, and asset value views |

Use the route inventory in [../README.md#api-reference](../README.md#api-reference) for the full endpoint matrix.

## Request and Error Behavior

| Behavior | Current implementation |
|----------|------------------------|
| Default response format | JSON via Spring MVC/Jackson |
| Null serialization | Null properties are omitted (`spring.jackson.default-property-inclusion=non_null`) |
| Validation failures | HTTP 400 with `fieldErrors` when applicable |
| Domain conflicts | HTTP 409 for duplicate records, invalid state, active-assignment conflicts, and seat exhaustion |
| Semantic license expiry failure | HTTP 422 (`UNPROCESSABLE_CONTENT`) |
| Unexpected failures | HTTP 500 with a generic message |

## Operational Constraints and Observations

1. No seed-data bootstrap was detected. The application starts with an empty database unless data is inserted manually.
2. The application currently exposes only Actuator `health` and `info` endpoints.
3. There is no Spring Security starter configured, so the API should be considered unsecured unless it is deployed behind a trusted network boundary or external gateway.
4. Development uses an ephemeral H2 database. Restarting in `dev` mode resets the data set.
5. Production defaults to Hibernate `update`, which is convenient for local iteration but should be reviewed before real production deployments.

## Logging Expectations

| Environment | Expected behavior |
|-------------|-------------------|
| `dev` | Detailed application logs plus service-level aspect logging for debugging and flow tracing |
| `prod` | Cleaner info-level application logs with noisier Hibernate and pool logs reduced |

## Recommended Deployment Guardrails

1. Put the API behind an authenticated gateway or add application-level security before exposing it externally.
2. Replace the default PostgreSQL credentials in `src/main/resources/docker/.env` for any shared environment.
3. Review `SPRING_JPA_HIBERNATE_DDL_AUTO` and prefer a migration-driven strategy for long-lived deployments.
4. Use Swagger UI and Actuator health checks as smoke-test surfaces after deployment.
