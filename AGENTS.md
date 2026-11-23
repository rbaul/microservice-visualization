# Microservice Visualization

## Overview

A Spring Boot application that visualizes microservice architectures by analyzing dependency relationships between applications. It loads project metadata (application dependency JSON files) from multiple sources (local filesystem, GitHub, Bitbucket), builds a dependency graph, detects topology connections and cyclic dependencies, and serves an Angular frontend for interactive visualization.

## Architecture

**Multi-module Gradle project** with two main subprojects:

| Module | Purpose |
|--------|---------|
| `microservice-visualization` | Spring Boot backend (Java 21, Spring Boot 3.2.x) |
| `microservice-visualization-webapp` | Angular 17 frontend (PrimeNG) |

A `demo/` folder contains sample microservice projects (`basket`, `customer`, `ordering`, `product`) used for local testing.

## Technology Stack

- **Backend**: Java 21, Spring Boot 3.2.3, Spring Data JPA, Spring WebFlux (for Bitbucket API client)
- **Database**: H2 in-memory (`create-drop` schema strategy)
- **Frontend**: Angular 17, PrimeNG, vis-network (topology visualization)
- **Build**: Gradle 8.5
- **Key Libraries**: JGraphT (cycle detection), ModelMapper, kohsuke github-api, Hypersistence Utils, SpringDoc OpenAPI

## Core Concepts

### Domain Model Hierarchy

```
ProjectVersion (top-level: e.g., "NC Muse")
  -> Project (a versioned snapshot: e.g., branch "master", "develop")
       -> Application (a microservice, library, or BOM)
            -> DependencyEntity (full resolved dependencies)
            -> DependencyManagement (BOM-managed dependencies)
       -> connections (topology: app-to-app communication links)
       -> dependencies (all inter-project dependency edges)
       -> librariesCycle (detected cyclic dependencies)
       -> groups, owners, tags
```

### Loader Architecture

Three `ProjectLoaderService` implementations load project data from different sources:

| Loader | Source | Key Mechanism |
|--------|--------|---------------|
| `LocalProjectLoaderService` | Filesystem folders | Reads versioned directories containing `applications/`, `libraries/`, `boms/`, `project-config.yaml` |
| `GithubProjectLoaderService` | GitHub repos | Uses kohsuke github-api, reads branches as versions |
| `BitbucketProjectLoaderService` | Bitbucket Server repos | Uses Spring HTTP Interface (`BitBucketV1Api`) with WebClient |

All loaders follow the same pattern:
1. List versions (branches/directories)
2. For each version, read `project-config.yaml`
3. Read application JSON files from `applications/`, `libraries/`, `boms/` folders
4. Convert to `Application` entities and build topology/dependency maps

### Topology Detection

Connections between microservices are inferred from dependency naming conventions:
- If app `ordering` depends on `customer-api`, and `customer` is a microservice, then `ordering -> customer` is a connection
- The API postfix pattern is configurable via `project-config.yaml` (`applicationApiPostfixes`, default: `-api`)

### Webhook Support

Two Bitbucket webhook formats are supported:
- **Post Webhook** (Cloud-style): `POST /api/v1/bitbucket/notifier` -- expects `push.changes[]` payload with `created`/`closed` flags
- **Native Webhook** (Data Center `repo:refs_changed`): `POST /api/v1/bitbucket/notifier/native` -- expects root-level `changes[]` with `ref.type` (`BRANCH`/`TAG`) and `type` (`ADD`/`UPDATE`/`DELETE`)

Both trigger automatic project updates on branch create/delete/modify events.

## REST API

| Endpoint | Method | Description |
|----------|--------|-------------|
| `api/v1/project-versions/search` | GET | List all project versions (paginated) |
| `api/v1/project-versions/{id}` | GET | Get project version details |
| `api/v1/projects/search?projectVersionId=` | GET | List projects for a version (paginated) |
| `api/v1/projects/{id}` | GET | Get full project with apps, connections, topology |
| `api/v1/projects/{id}/dependencies` | GET | Get all resolved dependencies |
| `api/v1/projects/{id}/dependencies/direct` | GET | Get direct dependencies only |
| `api/v1/projects/{id}/dependencies/implicit` | GET | Get implicit (non-BOM-managed) dependencies |
| `api/v1/applications/{id}` | GET | Get application details |
| `api/v1/sync/projects` | POST | Re-sync all projects from configured sources |
| `api/v1/bitbucket/notifier` | POST | Bitbucket Post Webhook endpoint (Cloud-style `push.changes` payload) |
| `api/v1/bitbucket/notifier/native` | POST | Bitbucket DC native webhook endpoint (`repo:refs_changed` payload) |

## Configuration

Application properties (`application.yaml` / `application-dev.yaml`):

```yaml
microservice-visualization:
  tags:                              # Map dependency coordinates to display tags
    "org.springframework.boot:spring-boot-starter-web": MVC
  loaders:
    local:
      enabled: true
      projects:
        - name: Demo
          folder: "docker/demo-project"
          default-version: 1.0.0
    github:
      enabled: true
      projects:
        - name: Demo
          project: rbaul
          repo: microservice-visualization-demo-project
    bitbucket:
      enabled: true
      projects:
        - name: NC Muse
          project: MUSE
          repo: nc-muse-architecture-ms
          url: https://bitbucket.example.com
          token: Bearer <token>
```

## Build & Run

```bash
# Build everything
./gradlew build

# Run the application (dev profile loads demo data)
# Set spring.profiles.active=dev
./gradlew :microservice-visualization:bootRun

# Frontend dev server (proxied to backend)
cd microservice-visualization-webapp && npm run dev
```

## Project Data Format

Application dependency JSON files (placed in `applications/`, `libraries/`, or `boms/` folders):

```json
{
  "name": "customer",
  "description": "Customer service",
  "group": "com.example",
  "version": "1.0.0",
  "owner": "team-a",
  "dependencies": [
    "com.example:customer-api:1.0.0",
    "org.springframework.boot:spring-boot-starter-web:3.2.0"
  ],
  "dependencyManagement": { ... },
  "tags": { "Spring Boot": "3.2.0" }
}
```

## Important Notes

- The H2 database is in-memory and `create-drop` -- all data is re-loaded on startup via `InitLoader`
- `SyncService.sync()` is called async on `ApplicationReadyEvent` and wipes + reloads all data
- Bitbucket API client uses `InsecureTrustManagerFactory` to skip SSL verification (internal Bitbucket Server)
- Tokens in `application-dev.yaml` should NOT be committed to public repositories
- The frontend is bundled as a Gradle dependency and served as static content, with `FrontendStaticController` forwarding `/ui/**` to `index.html`
