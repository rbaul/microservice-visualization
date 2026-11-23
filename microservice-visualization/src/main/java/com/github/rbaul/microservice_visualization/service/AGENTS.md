# service Package

Core business logic: project loading, synchronization, topology analysis, and CRUD operations.

## Top-Level Services

### `ProjectVersionService`
Manages `ProjectVersion` entities. Key operations:
- `loadProjects(properties)` -- iterates configured loaders, calls each `ProjectLoaderService.retrieve()`, and persists results. This is the main data ingestion entry point.
- `search(filter, pageable)` -- paginated search (filter parameter is accepted but currently unused in the query).
- `deleteAll()` -- wipes all project versions (used before full re-sync).

### `ProjectService`
Manages `Project` entities. Key operations:
- `get(id)` -- returns full project DTO
- `create(fullName, version)` -- creates a new version snapshot for an existing `ProjectVersion` by calling the appropriate loader
- `update(fullName, version)` -- re-fetches project data from the source and updates the existing entity
- `delete(fullName, version)` -- removes a version snapshot
- `getDependencies(id)` / `getDirectDependencies(id)` / `getImplicitDependencies(id)` -- different views of project dependencies (all resolved, direct only, non-BOM-managed only)

### `ApplicationService`
Simple read-only service for fetching individual `Application` details by ID.

### `SyncService`
Orchestrates full data refresh: deletes all existing data and reloads from configured properties. Marked `@Async` to run in background (triggered at startup by `InitLoader`).

### `AlgorithmUtils`
Utility for detecting cyclic dependencies using JGraphT's Paton cycle base algorithm. Converts a dependency adjacency map into a `SimpleGraph` and finds all simple cycles.

## Subpackage: `loaders/`

### `ProjectLoaderService` (abstract base)
Template for all project loaders. Provides:
- `retrieve(projectDetails)` -- orchestrates full project version loading
- `convertApplicationDependencyToApplication()` -- converts JSON-deserialized `ApplicationDependency` records to `Application` entities, including tag detection and dependency management resolution
- `createTopology()` -- infers app-to-app communication links from API-library naming conventions (e.g., `customer-api` dependency implies connection to `customer` microservice)
- `createDependenciesMap()` -- builds full inter-application dependency map
- `createLibDependenciesMap()` -- builds library-only dependency map (for cycle detection)
- `setApplicationToProject()` -- assembles a complete `Project` entity with apps, connections, cycles, groups, owners, tags
- `findTags()` -- matches application dependencies against configured tag patterns

### `ProjectLoaderFactory`
Registry of `ProjectLoaderService` implementations. Instantiated via constructor injection of all `ProjectLoaderService` beans, indexed by `ProjectLoaderType`.

### `ProjectLoaderType` (enum)
`local`, `github`, `bitbucket`

### `ProjectNotificationType` (enum)
`CREATED`, `DELETED`, `MODIFIED` -- used by webhook handlers.

### `loaders/local/LocalProjectLoaderService`
Reads from filesystem. Expects directory structure: `<folder>/<version>/applications/*.json`, `libraries/*.json`, `boms/*.json`, `project-config.yaml`.

### `loaders/github/GithubProjectLoaderService`
Uses kohsuke `github-api`. Connects anonymously for initial retrieval (public repos). Each branch = one project version.

### `loaders/bitbucket/BitbucketProjectLoaderService`
Uses Spring HTTP Interface proxy (`BitBucketV1Api`) with WebClient. Connects to Bitbucket Server REST API v1. Each branch = one project version.

### `loaders/bitbucket/BitBucketV1Api`
Spring `@HttpExchange` interface for Bitbucket Server REST API. Key endpoints: branches, tags, files listing, raw file content, commits, participants. Uses `InsecureTrustManagerFactory` for SSL (internal server). Static factory method `getBitBucketV1Api()` creates the proxy.

### `loaders/bitbucket/NotifierController`
REST controller at `api/v1/bitbucket` handling Bitbucket webhooks. Two endpoints:
- `POST /notifier` -- handles Post Webhook payloads (Cloud-style `push.changes` schema via `WebHookDto`). Dispatches to `ProjectService.create/delete/update` based on `created`/`closed` flags.
- `POST /notifier/native` -- handles Bitbucket Data Center native webhook payloads (`repo:refs_changed` event via `NativeWebHookDto`). Processes all changes in the payload, dispatching based on `ChangeType` (`ADD`/`DELETE`/`UPDATE`). Only acts on `BRANCH` ref types, ignores tags.

### `loaders/bitbucket/BitbucketConfig`, `loaders/github/GithubConfig`
Empty `@Configuration` classes (placeholder for future beans).

## Subpackage: `model/`

### `ApplicationDependency` (record)
JSON-deserialized model for application dependency files. Fields: `name`, `label`, `type`, `description`, `group`, `version`, `location`, `owner`, `tags`, `dependencies`, `fullDependencies`, `managementDependencies`, `dependencyManagement`.

### `ProjectConfig`
YAML-deserialized model for `project-config.yaml`. Fields: `applicationPostfix` (default: `""`), `applicationApiPostfixes` (default: `["-api"]`), `groups`, `owners`.

## Data Flow
```
InitLoader (ApplicationReadyEvent)
  -> SyncService.sync() [@Async]
       -> ProjectVersionService.deleteAll()
       -> ProjectVersionService.loadProjects(properties)
            -> for each enabled loader:
                 ProjectLoaderFactory.getByType(type)
                   -> ProjectLoaderService.retrieve(projectDetails)
                        -> retrieveProject() [list versions/branches]
                             -> for each version:
                                  read project-config.yaml
                                  read applications/*.json -> Application entities
                                  read libraries/*.json -> Application entities
                                  read boms/*.json -> Application entities
                                  setApplicationToProject() [topology, cycles, groups, owners]
                        -> build ProjectVersion with all Projects
                   -> save to database
```
