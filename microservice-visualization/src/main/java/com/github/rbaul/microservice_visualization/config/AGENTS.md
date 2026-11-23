# config Package

Configuration classes for the Spring Boot application.

## Classes

### `MicroserviceVisualizationProperties`
Central configuration holder bound to the `microservice-visualization` prefix. Contains:
- `tags` -- map of dependency coordinates (e.g., `org.springframework.boot:spring-boot-starter-web`) to display labels (e.g., `MVC`). Used during application loading to tag applications with the technologies they use.
- `loaders` -- map of `ProjectLoaderType` to `ProjectLoader` config, each containing an `enabled` flag and a list of `ProjectDetails` (name, description, URL, project, repo, token, folder, defaultVersion).

### `MicroserviceVisualizationConfig`
Provides a `ModelMapper` bean for entity-to-DTO mapping throughout the application.

### `AsyncConfig`
Enables `@EnableAsync` and configures a `ThreadPoolTaskExecutor` with core pool size 10, max 30, thread prefix `main-`. The async uncaught exception handler logs errors with method and parameter details. This supports the asynchronous project sync triggered at startup.

## Conventions
- Configuration properties use kebab-case in YAML (`microservice-visualization.loaders.bitbucket.projects[0].default-version`).
- `ProjectDetails` is a generic configuration POJO shared across all loader types -- not all fields are relevant to every loader (e.g., `folder` is local-only, `url`/`token` are Bitbucket-only).
