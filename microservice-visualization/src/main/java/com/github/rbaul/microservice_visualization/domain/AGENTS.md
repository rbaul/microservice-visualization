# domain Package

JPA entity model and Spring Data repositories.

## Subpackages

### `model/` -- JPA Entities

#### `ProjectVersion` (root aggregate)
Top-level entity representing a named project source (e.g., "NC Muse"). Has a unique `fullName` (e.g., `MUSE/nc-muse-architecture-ms`), embedded `LoaderDetails`, a set of `Project` snapshots (one per branch/version), and a `mainProject` reference (default version).

#### `Project`
A versioned snapshot of a project. Contains:
- `applications` -- set of `Application` entities (bidirectional `@OneToMany`, cascade all, orphan removal)
- `connections` -- topology map (`Map<String, List<String>>`) of app-to-app communication links
- `dependencies` -- full dependency map between all applications
- `librariesCycle` -- detected cyclic dependencies (stored as JSON via Hypersistence Utils)
- `groups`, `owners`, `tags`

Custom `setApplications()` manages the bidirectional relationship -- clears existing apps before adding new ones.

#### `Application`
Represents a microservice, library, or BOM. Key fields:
- `type` (`ApplicationType` enum: MICROSERVICE, LIBRARY, BOM)
- `dependencies` -- list of dependency strings (`group:artifact:version`)
- `fullDependencies` -- resolved `DependencyEntity` list
- `dependencyManagements` -- BOM-managed dependency list
- `tags` -- technology tags (e.g., `Spring Boot: 3.2.0`)
- `owners` -- set of owner names
- `group` (column: `artifact_group` to avoid SQL reserved word)

#### `DependencyEntity` (embeddable)
Resolved dependency with `groupId`, `artifactId`, `version`, and `implicit` flag (true if managed by a BOM).

#### `DependencyManagement` (embeddable)
A BOM dependency with its managed dependency list.

#### `ApplicationGroup`, `Owner` (embeddable)
Grouping and ownership metadata with name, description, and list of application names.

#### `LoaderDetails` (embeddable)
Stores the loader configuration used to retrieve a project: `url`, `project`, `repo`, `token`, `folder`, `loaderType`.

#### `ApplicationType` (enum)
`MICROSERVICE`, `LIBRARY`, `BOM` with string value lookup.

### `repository/` -- Spring Data JPA Repositories

| Repository | Entity | Custom Queries |
|------------|--------|---------------|
| `ProjectVersionRepository` | `ProjectVersion` | `findByFullName(String)` |
| `ProjectRepository` | `Project` | `findByProjectVersionId(int, Pageable)`, `findByProjectVersionFullNameAndVersion(String, String)` |
| `ApplicationRepository` | `Application` | None |

## Important Notes
- H2 in-memory database with `create-drop` strategy -- schema is rebuilt every restart
- `ImplicitNamingStrategyComponentPathImpl` is used for Hibernate naming (embeddable field names include component path)
- `librariesCycle` uses `@Type(JsonType.class)` from Hypersistence Utils for JSON column support
- Eager fetch on `Application.tags` (`@ElementCollection(fetch = FetchType.EAGER)`); everything else is lazy
