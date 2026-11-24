# Spring Boot 4.2.0 Migration — Design Document

> **Approach chosen:** A — direct jump from Spring Boot 3.2.3 to 4.2.0, then fix compile/runtime failures iteratively.

## Goal

Upgrade the `microservice-visualization` backend from Spring Boot 3.2.3 to Spring Boot 4.2.0, including all dependency and source-code changes required for the application to compile, start, pass tests, and behave identically to the current version.

## Why now / context

Spring Boot 3.x reaches end of open-source support in mid-2026, and the project wants to land on the current supported major line. The codebase is small (one backend module + an Angular webapp), so a direct jump is feasible with careful issue-by-issue fixes.

## Non-goals

- No frontend Angular/PrimeNG version changes.
- No functional changes to project loading, topology analysis, or REST endpoints.
- No database schema changes beyond what Hibernate 7 / H2 2.5 automatically produce.
- No migration to `RestClient` for the Bitbucket client unless the current WebClient/HTTP-Interface code breaks.

## Global constraints

- Java source/target compatibility must remain **Java 21** (Spring Boot 4.2 supports Java 17–27).
- Build tool must be upgraded to **Gradle 8.14+** (Spring Boot 4.2 requirement; currently 8.5). Decision: use the latest 8.x release available.
- Continue using H2 in-memory database with `create-drop`.
- Keep Angular webapp bundling into the backend jar unchanged.
- The `demo/` subprojects are in scope for a full migration; CI/Docker files will be reviewed and updated only if Spring Boot 4.2 forces a change.

## Key changes required

### 1. Build & dependency management

| Current | Target | Reason |
|---|---|---|
| `org.springframework.boot` plugin `3.2.3` | `4.2.0` | The migration target. |
| `io.spring.dependency-management` plugin `1.1.4` | compatible version (managed by Spring Boot plugin) | Plugin alignment with Spring Boot 4.2. |
| Gradle wrapper 8.5 | 8.14 or later (latest 8.x) | Spring Boot 4.2 minimum Gradle version. |
| `io.hypersistence:hypersistence-utils-hibernate-63:3.7.+` | `io.hypersistence:hypersistence-utils-hibernate-73:3.16.0` | Spring Boot 4.2 ships Hibernate 7.4.15. |
| `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.+` | `3.2.1` | Springdoc 2.x is for Spring Boot 3.x; 3.x is required for Spring Boot 4.x / Spring Framework 7.x. |
| `org.flywaydb:flyway-core` | **remove** | Flyway is disabled in `application.yaml`; in Spring Boot 4 the auto-configuration is modular and requires `spring-boot-starter-flyway`. Removing the unused dependency avoids silent migration failures. |
| — | `org.springframework.boot:spring-boot-h2console` | H2 console auto-configuration moved to the dedicated `spring-boot-h2console` module in Spring Boot 4; the old `spring-boot-starter-h2-console` convenience starter no longer exists. |
| `org.modelmapper.extensions:modelmapper-spring:3.2.+` | keep, or latest 3.2.x | No known Spring Boot 4 incompatibility. |
| `org.kohsuke:github-api:1.319` | keep for now | Uses Jackson 2 internally; Spring Boot 4 still manages Jackson 2 alongside Jackson 3, so it can coexist. Re-evaluate if runtime issues appear. |
| `org.jgrapht:jgrapht-core:1.5.2` | keep | No known incompatibility. |

### 2. Jackson 3 package migration

Spring Boot 4 uses Jackson 3 (`tools.jackson.*`) for the auto-configured `JsonMapper`/`ObjectMapper` bean. `jackson-annotations` remains in `com.fasterxml.jackson.annotation`, so annotation imports do **not** change.

Imports that must move:

- `com.fasterxml.jackson.databind.ObjectMapper` → `tools.jackson.databind.ObjectMapper`
- `com.fasterxml.jackson.core.JsonProcessingException` → `tools.jackson.core.JacksonException` (Jackson 3 renamed this exception; it is now unchecked / extends `RuntimeException`)
- `com.fasterxml.jackson.dataformat.yaml.YAMLFactory` → `tools.jackson.dataformat.yaml.YAMLFactory`
- Any `com.fasterxml.jackson.databind.annotation.*` annotations (e.g., `@JsonSerialize`, `@JsonDeserialize`, `@JsonView`) → `tools.jackson.databind.annotation.*`

Files affected:

- `service/loaders/ProjectLoaderService.java`
- `service/loaders/local/LocalProjectLoaderService.java`
- `service/loaders/github/GithubProjectLoaderService.java`
- `service/loaders/bitbucket/BitbucketProjectLoaderService.java`
- `test/java/.../DependenciesConvertTests.java`

Annotation imports in the following files stay as `com.fasterxml.jackson.annotation.*`:

- `service/loaders/bitbucket/dtos/WebHookDto.java`
- `service/loaders/bitbucket/dtos/NativeWebHookDto.java`
- `service/model/ApplicationDependency.java`
- `service/model/ProjectConfig.java`

### 3. Spring Boot 4 auto-configuration modularization

Spring Boot 4 moved many auto-config classes out of `spring-boot-autoconfigure` into dedicated starter modules. Because the project currently pulls raw `flyway-core` and expects the H2 console to auto-configure, we must either switch to the proper starters or remove the feature.

Decision: remove the unused Flyway dependency and add `spring-boot-h2console` to keep `/h2-console` working. Because the H2 console is no longer auto-enabled by default, also set `spring.h2.console.enabled: true` in `application.yaml`.

### 4. Configuration property review

Review both `application.yaml` and `application-dev.yaml` for renamed or removed properties. The project does not currently use any of the renamed/removed properties from Spring Boot 4 (e.g., `spring.http.client.*`, `server.forward-headers-strategy`). Expected YAML changes:

- Remove the `spring.flyway.enabled: false` block (Flyway dependency is being removed).
- Add/keep `spring.h2.console.enabled: true` (H2 console now requires explicit enablement plus the `spring-boot-h2console` module).

### 5. Compile/runtime remediation loop

After applying the above, run `./gradlew :microservice-visualization:build` and fix any remaining compile or test failures. Likely categories:

- Removed/renamed Spring APIs.
- Hibernate 7 type handling differences with `JsonType`.
- Jackson 3 default behavior differences in serialization.
- WebClient/HTTP-Interface API changes (currently expected to still compile, based on Spring Framework 7.1 Javadoc).

## Files to be modified

- `gradle/wrapper/gradle-wrapper.properties`
- `microservice-visualization/build.gradle`
- `microservice-visualization/src/main/resources/application.yaml`
- `microservice-visualization/src/main/java/.../service/loaders/ProjectLoaderService.java`
- `microservice-visualization/src/main/java/.../service/loaders/local/LocalProjectLoaderService.java`
- `microservice-visualization/src/main/java/.../service/loaders/github/GithubProjectLoaderService.java`
- `microservice-visualization/src/main/java/.../service/loaders/bitbucket/BitbucketProjectLoaderService.java`
- `microservice-visualization/src/test/java/.../DependenciesConvertTests.java`
- `demo/*/build.gradle` — the six demo subprojects are included in the Gradle build, so their Spring Boot plugin version should be aligned.
- `Dockerfile` and `.github/workflows/docker-image.yaml` — reviewed after the main build works; updated only if the base image, buildpack, or build command changes.

## Verification

1. `./gradlew :microservice-visualization:build` passes.
2. `./gradlew :microservice-visualization:bootRun` starts with `dev` profile and loads demo data without errors.
3. Smoke-test endpoints:
   - `GET /api/v1/project-versions/search`
   - `GET /api/v1/projects/search?projectVersionId=1`
   - `GET /swagger-ui.html` (OpenAPI UI)
   - `GET /h2-console` (H2 console)
4. Frontend integration: `./gradlew build` bundles the Angular app; `/ui` loads in browser.

## Risks and mitigations

| Risk | Mitigation |
|---|---|
| Jackson 3 default serialization differs from Jackson 2 and breaks JSON file parsing or API responses. | Run the existing tests; add a targeted test that parses `basket.json` and serializes a `Project` DTO if needed. |
| Hibernate 7 `JsonType` behavior changes for `librariesCycle`. | Build with H2 and verify startup; the column is JSON-backed and H2 2.5 supports it. |
| `github-api` or another transitive library conflicts with Jackson 3. | Keep Jackson 2 dependency management; library uses its own mapper. Re-evaluate only if runtime errors appear. |
| Demo subprojects may use Jackson 2 imports or deprecated Spring APIs. | Include `demo/` in the Jackson import sweep and run the full `./gradlew build` after alignment. |
| Bitbucket client still disables SSL certificate validation (`InsecureTrustManagerFactory`). | Out of scope for this migration; flag as a security risk to revisit separately. |
| Gradle 8.14+ plugin/feature incompatibilities. | Pick latest 8.x wrapper (8.14 or newer) rather than 9.x to minimize disruption. |
| Springdoc 3.x changes default OpenAPI path or UI path. | Verify `/swagger-ui.html` and `/v3/api-docs` still serve. |

## Approaches considered

| Approach | Trade-off | Why not chosen |
|---|---|---|
| A — direct jump to 4.2.0 | Fastest; surfaces many breakages at once | **Chosen** because the codebase is small and the major breaking items (Jackson 3, Hibernate 7, Gradle, starters) are already known. |
| B — stepwise 3.2 → 3.5 → 4.0 → 4.1 → 4.2 | Easier to bisect; more commits/iterations | Rejected because the user prefers the direct path and the project does not use removed 3.x deprecations that a 3.5 stop would catch. |
| C — audit-and-rebuild | Most thorough; overkill | Rejected because there is no dead code or unsupported dependencies to purge beyond Flyway. |
