# demo Package

Sample microservice architecture used to demonstrate and test the visualization tool. Contains **no application code** -- only Gradle build files that declare dependencies, which are then extracted into JSON files by the `createDependencyFile` Gradle task.

## Architecture

A simple e-commerce microservice topology:

```
basket  ──depends──> basket-api (library)
customer ──depends──> basket-api (library)
ordering (standalone, no API dependencies)
product  (standalone, no API dependencies)
app-bom  (BOM managing shared third-party versions)
```

Topology inference: since `customer` depends on `basket-api`, the tool infers a `customer -> basket` communication link.

## Projects

| Project | Type | Owner | Spring Boot | Description |
|---------|------|-------|-------------|-------------|
| `basket` | microservice | Team 1 | 2.7.10 | Basket service with AMQP, JPA, Web, Actuator |
| `basket-api` | library | Team 1 | 2.7.10 | Basket API DTOs shared with consumers |
| `customer` | microservice | Team 1 | 2.7.5 | Customer service with MongoDB, AMQP |
| `ordering` | microservice | Team 2 | 3.0.5 | Ordering service (web + validation only) |
| `product` | microservice | Team 3 | 3.0.5 | Product service with MongoDB, AMQP |
| `app-bom` | bom | Team 1 | N/A | BOM managing commons-collections4, commons-io, jgrapht, guava |

## Gradle Plugin: `dependencies-util.gradle`

Located at `plugins/gradle/dependencies-util.gradle`, applied by each demo project via `apply from: '../../plugins/gradle/dependencies-util.gradle'`.

This plugin is the **data extraction mechanism** that bridges real Gradle projects to the visualization tool. It provides:

### `createDependencyFile` task
Generates a JSON file for each project containing:
- `name`, `label`, `group`, `version`, `description`, `type`, `owner`
- `tags` -- auto-detected: Java version, Gradle version, dependency override setting
- `dependencies` -- first-level module dependencies (resolved, `group:artifact:version` format)
- `fullDependencies` -- all transitive dependencies (resolved)
- `managementDependencies` -- BOM coordinates used
- `dependencyManagement` -- map of BOM coordinates to their managed dependency versions

### Key functions
- `getFirstLevelModuleDependencies()` -- resolves `runtimeClasspath` first-level deps, including expanding project module dependencies into their children
- `getAllDependencies()` -- resolves all transitive artifacts from `runtimeClasspath`
- `getManagementDependencies()` -- extracts imported BOMs and their managed dependency versions
- `getManagementDependenciesExplicit()` -- extracts explicitly declared version overrides (used for BOM-type projects)

### Project `ext` properties used
- `name` -- display label (defaults to project name, title-cased)
- `ownerName` -- team/person owning the application
- `type` -- `microservice`, `library`, or `bom`
- `dependencyFileLocation` -- output directory for the JSON file (optional)
- `dependencyVersions` -- map of version placeholders for BOM projects

## Generated Output: `docker/demo-project/`

The `createDependencyFile` task output is stored in `docker/demo-project/`, structured for the `LocalProjectLoaderService`:

```
docker/demo-project/
  1.0.0/
    project-config.yaml          # Groups, owners, API postfix config
    applications/
      basket.json, customer.json, ordering.json, product.json
    libraries/
      basket-api.json, common-lib.json, customer-api.json,
      ordering-api.json, product-api.json
  2.0.0/
    project-config.yaml
    applications/
      basket.json, customer.json, ordering.json, product.json
    boms/
      app-bom.json
    libraries/
      basket-api.json, customer-api.json, ordering-api.json,
      product-api.json
```

### `project-config.yaml` format
```yaml
applicationPostfix:                # Suffix stripped from app names (empty)
applicationApiPostfixes:
  - -api                           # API library suffix convention
groups:
  - name: Group Demo
    applicationNames: [basket, ordering]
owners:
  - name: Team 1
    applicationNames: [basket, customer]
  - name: Team 2
    applicationNames: [product, ordering]
```

## How to Regenerate

```bash
# From project root -- generates JSON files for all demo subprojects
./gradlew :basket:createDependencyFile
./gradlew :customer:createDependencyFile
./gradlew :ordering:createDependencyFile
./gradlew :product:createDependencyFile
./gradlew :basket-api:createDependencyFile
./gradlew :app-bom:createDependencyFile
```

Output JSON files are written to the project root by default (set `dependencyFileLocation` ext property to redirect).

## Important Notes
- The demo projects are included in the root `settings.gradle` with custom `projectDir` mappings
- Version differences across projects (Spring Boot 2.7 vs 3.0) are intentional -- they demonstrate the tool's ability to visualize heterogeneous architectures
- The `docker/demo-project/` output has two versions (`1.0.0`, `2.0.0`) to demonstrate the multi-version visualization feature
- `1.0.0` has no BOMs; `2.0.0` adds `app-bom` and removes `common-lib`
