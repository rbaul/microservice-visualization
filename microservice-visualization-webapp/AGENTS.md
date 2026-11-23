# microservice-visualization-webapp

Angular 17 frontend for the Microservice Visualization application.

## Technology Stack
- Angular 17 (standalone components, no NgModules)
- PrimeNG 17 (UI component library: tables, toolbars, dropdowns, dialogs)
- vis-network 9.x (graph/topology visualization)
- RxJS, Lodash
- Base href: `/ui` (served by backend's `FrontendStaticController`)

## Project Structure

```
src/app/
  api/                        # API services and models
  project-versions/           # Project version list page
  projects/                   # Projects list page (with datasource)
  project-view/               # Single project view (topology + details)
  project-topology/           # vis-network graph component
  project-dependencies/       # Dependencies table view
  application-view/           # Single application detail view
  utils/                      # Shared utilities (pagination, error handling)
```

## Routing

| Route | Component | Description |
|-------|-----------|-------------|
| `projects` | `ProjectsComponent` | List of project versions (home page) |
| `projects/:id/versions` | `ProjectVersionsComponent` | List of version snapshots for a project version |
| `projects/:id` | `ProjectViewComponent` | Full project view with topology graph |
| `projects/:id/dependencies` | `ProjectDependenciesComponent` | Dependency analysis table |
| `**` | redirect to `projects` | Catch-all |

## API Layer (`api/`)

Three API services matching the backend REST controllers:

| Service | Backend Endpoint | Methods |
|---------|-----------------|---------|
| `ProjectVersionApiService` | `api/v1/project-versions` | `get`, `delete`, `getPageable` |
| `ProjectApiService` | `api/v1/projects` | `get`, `delete`, `getPageable`, `getDependencies`, `getDirectDependencies`, `getImplicitDependencies` |
| `ApplicationApiService` | `api/v1/applications` | `get` |

`ProjectVersionApiService` extends `PageableApiService` for reusable paginated data fetching.

## Key Component: `ProjectTopologyComponent`

The core visualization component using vis-network. Features:
- Renders microservices and libraries as network nodes with topology/dependency edges
- Three view modes: `ALL` (connections + dependencies), `MICROSERVICES` (connections only), `DEPENDENCIES` (dependency edges only)
- **Group clustering**: application groups are rendered as collapsible clusters with dashed-border visual overlays (drawn in `beforeDrawing` canvas callback)
- **Filtering**: by application name, owner, type, or cycle issue
- **Navigation**: zoom in/out, fit, move-to-app
- **Interaction**: click to collapse groups, double-click to expand, drag groups, right-click context menu, hover highlights
- **Cycle detection display**: cyclic library dependencies shown as selectable issues

## Development

```bash
# Dev server with backend proxy
npm run dev

# Dev server with mock JSON server
npm run dev-mock

# Build for production (output bundled into backend jar)
npm run build
```

## Conventions
- All components are standalone (Angular 17 style, no NgModules)
- PrimeNG components imported directly in each component's `imports` array
- API base URLs are relative (proxied to backend in dev, same origin in production)
- Models defined alongside services in `api/*.model.ts`
- Pagination utilities in `utils/` (`Page`, `PageableDataSource`, `PageableApiService`)
