# Spring Boot 4.2.0 Migration — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:executing-plans` (native execution in this session) to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Upgrade the `microservice-visualization` backend (and included demo subprojects) from Spring Boot 3.2.3 to Spring Boot 4.2.0, with all source and build changes required to compile, test, and start successfully.

**Architecture:** Direct version bump across Gradle, Spring Boot plugin, and direct dependencies; migrate Jackson 2 imports to Jackson 3 where the auto-configured `JsonMapper` bean is consumed; switch to the Spring Boot 4 module-based starters for Flyway/H2 console; fix any compile/runtime regressions in a tight loop.

**Tech Stack:** Java 21, Gradle 8.14+, Spring Boot 4.2.0, Spring Framework 7.1, Hibernate 7.4, Jackson 3, H2, Angular 17 (unchanged).

**Spec:** `docs/superpowers/specs/2026-11-23-spring-boot-4.2.0-migration-design.md`

## Global Constraints

- Java source/target compatibility must remain **Java 21**.
- Build tool must be **Gradle 8.14+** (latest 8.x).
- Continue using H2 in-memory database with `create-drop`.
- Keep Angular webapp bundling into the backend jar unchanged.
- The six `demo/` subprojects are in scope for version alignment.
- No functional changes to project loading, topology analysis, or REST endpoints.

## Review Focus

| Input / condition | Expected behavior | Test that pins it |
|---|---|---|
| Local JSON dependency files (e.g., `basket.json`) | Still deserialize correctly with Jackson 3. | Task 7: `DependenciesConvertTests` parses `basket.json` without error. |
| H2 in-memory startup | Application starts and schema is created. | Task 8: `bootRun` with `dev` profile starts without Hibernate/JPA errors. |
| `/h2-console` endpoint | H2 console is reachable after modularization. | Task 8: `GET /h2-console` returns 200. |
| `/swagger-ui.html` endpoint | OpenAPI UI is reachable with Springdoc 3.x. | Task 8: `GET /swagger-ui.html` returns 200. |
| Bitbucket loader WebClient/HTTP Interface | Still creates proxy and can list branches/files. | Task 8: smoke test or code inspection confirms `HttpServiceProxyFactory.builderFor(...)` compiles and runs. |

---

### Task 1: Upgrade Gradle wrapper to 8.14+

**Files:**
- Modify: `gradle/wrapper/gradle-wrapper.properties`
- Verify: wrapper jar in `gradle/wrapper/`

**Interfaces:**
- Produces: Gradle wrapper compatible with Spring Boot 4.2 Gradle plugin.

- [ ] **Step 1: Update `gradle-wrapper.properties` to latest 8.x**

  Change the `distributionUrl` line to Gradle 8.14 (or the latest available 8.x patch, e.g., `https://services.gradle.org/distributions/gradle-8.14-bin.zip`).

- [ ] **Step 2: Run wrapper task to sync the wrapper jar**

  Run: `./gradlew wrapper --gradle-version 8.14`
  Expected: wrapper downloads successfully, command exits 0.

- [ ] **Step 3: Verify Gradle version**

  Run: `./gradlew --version`
  Expected: Gradle version is 8.14 or later.

- [ ] **Step 4: Commit**

  ```bash
  git add gradle/wrapper/gradle-wrapper.properties gradle/wrapper/gradle-wrapper.jar
  git commit -m "chore: upgrade Gradle wrapper to 8.14 for Spring Boot 4.2"
  ```

---

### Task 2: Update backend `build.gradle` for Spring Boot 4.2

**Files:**
- Modify: `microservice-visualization/build.gradle`

**Interfaces:**
- Consumes: Gradle 8.14+ wrapper from Task 1.
- Produces: dependency set and plugin versions aligned with Spring Boot 4.2.

- [ ] **Step 1: Update Spring Boot and dependency-management plugin versions**

  ```groovy
  plugins {
      id 'java'
      id 'org.springframework.boot' version '4.2.0'
      id 'io.spring.dependency-management' version '1.1.7'
      id 'maven-publish'
  }
  ```

  Version `1.1.7` is the dependency-management plugin version aligned with Spring Boot 4.2.

- [ ] **Step 2: Update direct dependencies**

  - `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.+` → `3.2.1` (pin to this specific version).
  - `io.hypersistence:hypersistence-utils-hibernate-63:3.7.+` → `io.hypersistence:hypersistence-utils-hibernate-73:3.16.0` (pin to this specific version).
  - Remove `org.flywaydb:flyway-core`.
  - Add `org.springframework.boot:spring-boot-h2console` (the old `spring-boot-starter-h2-console` starter no longer exists in Spring Boot 4).
  - Keep `modelmapper-spring:3.2.+`, `github-api:1.319`, `jgrapht-core:1.5.2`.

- [ ] **Step 3: Run a dry dependency resolution check**

  Run: `./gradlew :microservice-visualization:dependencies --configuration runtimeClasspath`
  Expected: no unresolved dependencies; Hibernate 7.4, Jackson 3, Springdoc 3.x present.

- [ ] **Step 4: Commit**

  ```bash
  git add microservice-visualization/build.gradle
  git commit -m "chore: upgrade backend dependencies for Spring Boot 4.2"
  ```

---

### Task 3: Migrate Jackson imports to Jackson 3

**Files:**
- Modify:
  - `microservice-visualization/src/main/java/.../service/loaders/ProjectLoaderService.java`
  - `microservice-visualization/src/main/java/.../service/loaders/local/LocalProjectLoaderService.java`
  - `microservice-visualization/src/main/java/.../service/loaders/github/GithubProjectLoaderService.java`
  - `microservice-visualization/src/main/java/.../service/loaders/bitbucket/BitbucketProjectLoaderService.java`
  - `microservice-visualization/src/test/java/.../DependenciesConvertTests.java`

**Interfaces:**
- Consumes: Jackson 3 on the classpath from Task 2.
- Produces: source files that reference the Spring Boot 4 auto-configured `JsonMapper`/`ObjectMapper` bean type.

- [ ] **Step 1: Replace Jackson databind/core/yaml imports**

  In each file, replace:
  - `com.fasterxml.jackson.databind.ObjectMapper` → `tools.jackson.databind.ObjectMapper`
  - `com.fasterxml.jackson.core.JsonProcessingException` → `tools.jackson.core.JacksonException` (Jackson 3 renamed this exception; it is now unchecked / extends `RuntimeException`)
  - `com.fasterxml.jackson.dataformat.yaml.YAMLFactory` → `tools.jackson.dataformat.yaml.YAMLFactory`
  - Any `com.fasterxml.jackson.databind.annotation.*` imports → `tools.jackson.databind.annotation.*`

  Leave `com.fasterxml.jackson.annotation.*` imports unchanged.

- [ ] **Step 2: Verify no remaining Jackson 2 databind/core/yaml imports exist**

  Search the backend source tree and `demo/` for:
  - `com.fasterxml.jackson.databind.ObjectMapper`
  - `com.fasterxml.jackson.core.JsonProcessingException`
  - `com.fasterxml.jackson.dataformat.yaml.YAMLFactory`
  - `com.fasterxml.jackson.databind.annotation.*`

  Leave `com.fasterxml.jackson.annotation.*` (e.g., `@JsonProperty`, `@JsonIgnoreProperties`, `@JsonValue`) unchanged.

- [ ] **Step 3: Commit**

  ```bash
  git add microservice-visualization/src/main/java/... microservice-visualization/src/test/java/...
  git commit -m "refactor: migrate Jackson 2 imports to Jackson 3"
  ```

---

### Task 4: Update `application.yaml`

**Files:**
- Modify: `microservice-visualization/src/main/resources/application.yaml`

**Interfaces:**
- Consumes: dependency changes from Task 2.
- Produces: config file without stale Flyway property.

- [ ] **Step 1: Remove the disabled Flyway block**

  Delete these lines from `application.yaml` (and `application-dev.yaml` if present):
  ```yaml
  flyway:
    enabled: false
  ```

- [ ] **Step 2: Ensure H2 console is explicitly enabled**

  Confirm `application.yaml` contains:
  ```yaml
  spring:
    h2:
      console:
        enabled: true
        path: /h2-console
  ```

- [ ] **Step 3: Search all YAML files for renamed/removed Spring Boot 4 properties**

  Search `application.yaml` and `application-dev.yaml` for keys known to be renamed/removed in Spring Boot 4.2:
  ```bash
  grep -R "spring.http.client\|server.forward-headers-strategy\|spring.flyway\|spring.jackson.parser\|spring.jackson.read\|spring.jackson.write" microservice-visualization/src/main/resources
  ```
  Update or remove any matches.

- [ ] **Step 4: Commit**

  ```bash
  git add microservice-visualization/src/main/resources/application.yaml microservice-visualization/src/main/resources/application-dev.yaml
  git commit -m "chore: update YAML config for Spring Boot 4.2"
  ```

---

### Task 5: Align demo subproject Spring Boot versions

**Files:**
- Modify:
  - `demo/app-bom/build.gradle`
  - `demo/basket-api/build.gradle`
  - `demo/basket/build.gradle`
  - `demo/customer/build.gradle`
  - `demo/ordering/build.gradle`
  - `demo/product/build.gradle`

**Interfaces:**
- Consumes: Gradle 8.14+ from Task 1.
- Produces: demo subprojects using Spring Boot 4.2.0 plugin.

- [ ] **Step 1: Update each demo `build.gradle` Spring Boot plugin version**

  In each file, change the `org.springframework.boot` plugin version to `4.2.0` and `io.spring.dependency-management` to `1.1.7`.

- [ ] **Step 2: Migrate any Jackson 2 imports in demo source code**

  Search `demo/` for the same Jackson 2 imports listed in Task 3 and migrate them to Jackson 3. Demo JSON/YAML parsing code is the most likely to need changes.

- [ ] **Step 3: Resolve demo build issues**

  Run: `./gradlew :basket:build :basket-api:build :customer:build :ordering:build :product:build :app-bom:build`
  Expected: all demo builds succeed. Fix any compile or dependency issues that surface.

- [ ] **Step 4: Commit**

  ```bash
  git add demo/
  git commit -m "chore: align demo subprojects with Spring Boot 4.2"
  ```

---

### Task 6: Compile backend and fix compilation errors

**Files:**
- TBD depending on compile output.

**Interfaces:**
- Consumes: dependency changes from Task 2 and Jackson import changes from Task 3.
- Produces: backend module that compiles cleanly.

- [ ] **Step 1: Compile main and test sources**

  Run: `./gradlew :microservice-visualization:compileJava :microservice-visualization:compileTestJava`
  Expected: identify all compilation failures.

- [ ] **Step 2: Fix each compilation failure**

  Common expected fixes:
  - Remove or replace deprecated Spring/Hibernate APIs.
  - Update `HttpServiceProxyFactory` usage if Spring Framework 7.1 changed the builder API.
  - Update any `ObjectMapper` bean references to `tools.jackson.databind.ObjectMapper`.

- [ ] **Step 3: Re-run until clean**

  Run: `./gradlew :microservice-visualization:compileJava :microservice-visualization:compileTestJava`
  Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

  ```bash
  git add microservice-visualization/
  git commit -m "fix: resolve Spring Boot 4.2 compilation errors"
  ```

---

### Task 7: Run tests and fix runtime/serialization regressions

**Files:**
- TBD depending on test output.
- Likely: `microservice-visualization/src/test/java/.../DependenciesConvertTests.java`

**Interfaces:**
- Consumes: backend that compiles from Task 6.
- Produces: passing unit/integration tests.

- [ ] **Step 1: Run backend tests**

  Run: `./gradlew :microservice-visualization:test`
  Expected: identify test/runtime failures.

- [ ] **Step 2: Fix Jackson 3 serialization issues**

  If `DependenciesConvertTests` fails because of changed defaults, add explicit Jackson configuration or adjust the test assertion. Prefer configuring the auto-configured `JsonMapper` via `JsonMapperBuilderCustomizer` rather than replacing the bean.

- [ ] **Step 3: Fix Hibernate 7 JSON type issues**

  If `librariesCycle` JSON mapping fails with `JsonType`, verify the `hypersistence-utils-hibernate-73` artifact is resolved and that H2 JSON column type still works. If necessary, switch the field to `@JdbcTypeCode(SqlTypes.JSON)`.

- [ ] **Step 4: Re-run until all tests pass**

  Run: `./gradlew :microservice-visualization:test`
  Expected: BUILD SUCCESSFUL, all tests pass.

- [ ] **Step 5: Commit**

  ```bash
  git add microservice-visualization/
  git commit -m "fix: resolve Spring Boot 4.2 test and runtime regressions"
  ```

---

### Task 8: Verify runtime behavior with dev profile

**Files:**
- None; verification only.

**Interfaces:**
- Consumes: passing backend from Task 7.
- Produces: confidence that the application starts and key endpoints work.

- [ ] **Step 1: Start the application with dev profile**

  Run: `./gradlew :microservice-visualization:bootRun --args='--spring.profiles.active=dev'`
  Wait for: `Started MicroserviceVisualizationApplication` log line and no ERROR/FATAL logs.

- [ ] **Step 2: Smoke test REST endpoints with curl**

  Start `bootRun` in the background, wait for startup, then run concrete curl commands:

  ```bash
  ./gradlew :microservice-visualization:bootRun --args='--spring.profiles.active=dev' &
  BOOT_PID=$!
  sleep 60
  curl -sf http://localhost:8080/api/v1/project-versions/search >/dev/null
  curl -sf http://localhost:8080/api/v1/projects/search?projectVersionId=1 >/dev/null
  curl -sf http://localhost:8080/swagger-ui.html >/dev/null
  curl -sf http://localhost:8080/h2-console >/dev/null
  curl -sf http://localhost:8080/ui >/dev/null
  kill $BOOT_PID
  wait $BOOT_PID 2>/dev/null || true
  ```

  Expected: all curl commands exit 0 and the application log contains no ERROR/FATAL lines.

- [ ] **Step 3: Shut down the application if still running**

  Ensure the background `bootRun` process is terminated.

- [ ] **Step 4: Commit any runtime fixes**

  If any endpoint failed and required a code/config change, commit those separately.

---

### Task 9: Review Docker and CI configuration

**Files:**
- Review: `Dockerfile`, `.github/workflows/docker-image.yaml`
- Modify only if required.

**Interfaces:**
- Consumes: working runtime from Task 8.
- Produces: Docker/CI configuration compatible with Spring Boot 4.2 if changes are needed.

- [ ] **Step 1: Inspect `Dockerfile` for base image / Java version / build command**

  Check whether the base image Java version or buildpack settings need updating for Spring Boot 4.2. Note: Spring Boot 4.2 default CNB builder changed to Ubuntu 26.04; this only matters if the Dockerfile uses `bootBuildImage` or Paketo buildpacks.

- [ ] **Step 2: Inspect `.github/workflows/docker-image.yaml`**

  Verify the workflow uses the upgraded Gradle wrapper and Java 21. Update only if it pins an older Gradle or Java version.

- [ ] **Step 3: Note security risks that are out of scope**

  `BitBucketV1Api.getBitBucketV1Api()` continues to use `InsecureTrustManagerFactory` to bypass SSL verification. This is unchanged in the migration but should be recorded as a future security hardening item.

- [ ] **Step 4: Apply only necessary changes**

  If no Spring Boot 4.2 incompatibility is found, leave the files unchanged. If a change is required, make the minimal edit and commit.

  ```bash
  git add Dockerfile .github/workflows/docker-image.yaml
  git commit -m "chore: update Docker/CI for Spring Boot 4.2"
  ```

  (If no changes were needed, skip the commit.)

---

## Self-Review

**Spec coverage:** All design-doc items are represented: Gradle upgrade (Task 1), backend dependencies (Task 2), Jackson migration (Task 3), YAML cleanup (Task 4), demo alignment (Task 5), compile fix loop (Task 6), test/runtime fix loop (Task 7), runtime smoke tests (Task 8), Docker/CI review (Task 9).

**Step scan:** Each step names one action and one checkable result. No placeholder steps.

**Type consistency:** All tasks refer to `ObjectMapper`/`JsonMapper` consistently as the Jackson 3 type.

**Review focus:** Each concern has a corresponding test/verification step in the table above.

**Proportion:** The plan is longer than the code it describes because the migration is exploratory; unknown failures are handled in Tasks 6 and 7 with bounded fix loops.
