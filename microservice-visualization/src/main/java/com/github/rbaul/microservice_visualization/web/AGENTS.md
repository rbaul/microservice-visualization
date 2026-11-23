# web Package

REST API controllers, DTOs, error handling, and frontend static content serving.

## Subpackage: `controller/`

### `ProjectVersionController`
`api/v1/project-versions` -- CRUD for project versions. `create` and `update` are stubbed (return null). `search` supports pagination.

### `ProjectController`
`api/v1/projects` -- CRUD for projects plus dependency analysis endpoints:
- `GET /{id}/dependencies` -- all resolved dependencies with usage tracking
- `GET /{id}/dependencies/direct` -- direct dependencies only
- `GET /{id}/dependencies/implicit` -- dependencies not managed by any BOM

### `ApplicationController`
`api/v1/applications` -- read-only, single `GET /{id}` endpoint.

### `SyncController`
`api/v1/sync` -- `POST /projects` triggers full async re-sync of all project data.

## Subpackage: `dto/`

DTOs follow a naming pattern:
- `*Dto` -- full detail transfer object
- `*LiteDto` -- lightweight version for list/search results
- `*FullDto` -- expanded version with all nested data

| DTO | Purpose |
|-----|---------|
| `ProjectVersionDto`, `ProjectVersionLiteDto` | Project version data |
| `ProjectDto`, `ProjectLiteDto` | Project snapshot data |
| `ProjectDependenciesDto` | Dependencies analysis result |
| `ApplicationFullDto`, `ApplicationLiteDto` | Application details |
| `ApplicationGroupDto` | Application grouping info |
| `DependencyDto` | Dependency with usage tracking (`usageOf` list) |
| `DependencyManagementDto` | BOM dependency management info |
| `OwnerDto` | Ownership info |

### `dto/errors/`
- `ErrorDto` -- standard error response with `errorCode` and `message`
- `ValidationErrorDto` -- validation error with field details
- `ErrorCodes` -- enum of error codes (`UNKNOWN`, `NOT_FOUND`, etc.)

## Subpackage: `handler/`

### `GlobalErrorHandlers`
`@RestControllerAdvice` with two handlers:
- `Exception` -> 500 with `UNKNOWN` error code
- `EmptyResultDataAccessException` -> 404 with `NOT_FOUND` error code

## Subpackage: `frontend/`

### `FrontendStaticController`
`@Controller` (not `@RestController`) that forwards all `/ui/**` requests to `index.html` (Angular SPA routing support via `forward:/`).

## Conventions
- All REST controllers use `@RestController` with `@RequestMapping("api/v1/...")` prefix
- Pagination via Spring Data's `@PageableDefault`
- Entity-to-DTO mapping uses ModelMapper (injected via `MicroserviceVisualizationConfig`)
- Controllers are thin -- delegate immediately to service layer
