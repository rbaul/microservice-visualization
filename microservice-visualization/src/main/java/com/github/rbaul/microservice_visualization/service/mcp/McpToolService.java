package com.github.rbaul.microservice_visualization.service.mcp;

import com.github.rbaul.microservice_visualization.config.MicroserviceVisualizationProperties;
import com.github.rbaul.microservice_visualization.domain.model.ApplicationType;
import com.github.rbaul.microservice_visualization.domain.model.ProjectVersion;
import com.github.rbaul.microservice_visualization.domain.repository.ProjectVersionRepository;
import com.github.rbaul.microservice_visualization.service.ApplicationService;
import com.github.rbaul.microservice_visualization.service.ProjectService;
import com.github.rbaul.microservice_visualization.service.ProjectVersionService;
import com.github.rbaul.microservice_visualization.web.dto.ApplicationFullDto;
import com.github.rbaul.microservice_visualization.web.dto.ApplicationLiteDto;
import com.github.rbaul.microservice_visualization.web.dto.DependencyDto;
import com.github.rbaul.microservice_visualization.web.dto.ProjectDependenciesDto;
import com.github.rbaul.microservice_visualization.web.dto.ProjectDto;
import com.github.rbaul.microservice_visualization.web.dto.ProjectLiteDto;
import com.github.rbaul.microservice_visualization.web.dto.ProjectVersionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class McpToolService {

    private static final String DIRECT = "DIRECT";
    private static final String FULL = "FULL";
    private static final String IMPLICIT = "IMPLICIT";
    private static final String EXPLICIT = "EXPLICIT";

    private final ProjectVersionService projectVersionService;
    private final ProjectService projectService;
    private final ApplicationService applicationService;
    private final ProjectVersionRepository projectVersionRepository;
    private final MicroserviceVisualizationProperties properties;

    @McpTool(name = "list_project_versions", description = "List all available project versions with optional text filter and pagination")
    public PagedModel<ProjectVersionDto> listProjectVersions(
            @McpToolParam(description = "Optional text filter for project version name or description", required = false) String filter,
            @McpToolParam(description = "Page number (0-based), defaults to 0", required = false) Integer page,
            @McpToolParam(description = "Page size, defaults to 20", required = false) Integer size) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;
        return new PagedModel<>(projectVersionService.search(filter, PageRequest.of(pageNum, pageSize)));
    }

    @McpTool(name = "get_project_version", description = "Get a project version by its numeric ID or display name. Returns the project version with its list of project snapshots (branches/versions).")
    @Transactional(readOnly = true)
    public ProjectVersionDto getProjectVersion(
            @McpToolParam(description = "Numeric ID of the project version", required = false) Integer id,
            @McpToolParam(description = "Display name of the project version (e.g., 'Demo Github project')", required = false) String name) {
        if (id != null) {
            return projectVersionService.get(id);
        }
        if (StringUtils.hasText(name)) {
            ProjectVersion pv = projectVersionRepository.findByName(name)
                    .orElseThrow(() -> new RuntimeException("No project version found with name: " + name));
            return projectVersionService.get(pv.getId());
        }
        throw new IllegalArgumentException("Either 'id' or 'name' must be provided");
    }

    @McpTool(name = "list_projects", description = "List project snapshots (branches/versions) for a given project version, with pagination")
    public PagedModel<ProjectLiteDto> listProjects(
            @McpToolParam(description = "ID of the project version", required = true) int projectVersionId,
            @McpToolParam(description = "Page number (0-based), defaults to 0", required = false) Integer page,
            @McpToolParam(description = "Page size, defaults to 20", required = false) Integer size) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;
        return new PagedModel<>(projectService.search(projectVersionId, PageRequest.of(pageNum, pageSize)));
    }

    @McpTool(name = "get_project", description = "Get full project details including applications, topology connections, dependency map, cyclic dependencies, groups, owners, and tags")
    public ProjectDto getProject(
            @McpToolParam(description = "ID of the project", required = true) int projectId) {
        return projectService.get(projectId);
    }

    @McpTool(name = "get_application", description = "Get full application details including its direct dependencies and dependency management (BOM) information")
    public ApplicationFullDto getApplication(
            @McpToolParam(description = "ID of the application", required = true) long applicationId) {
        return applicationService.get(applicationId);
    }

    @McpTool(name = "get_project_dependencies", description = "Get project dependencies with optional keyword search and dependency type filter. Each dependency includes which applications use it (usageOf).")
    public ProjectDependenciesDto getProjectDependencies(
            @McpToolParam(description = "ID of the project", required = true) int projectId,
            @McpToolParam(description = "Optional search keyword to filter by packageName or artifactName (case-insensitive contains match)", required = false) String query,
            @McpToolParam(description = "Dependency type: DIRECT (declared dependencies, default), FULL (all resolved), IMPLICIT (not BOM-managed), EXPLICIT (BOM-managed only)", required = false) String dependencyType) {

        String type = StringUtils.hasText(dependencyType) ? dependencyType.toUpperCase(Locale.ROOT) : DIRECT;
        ProjectDependenciesDto result = switch (type) {
            case FULL -> projectService.getDependencies(projectId);
            case IMPLICIT -> projectService.getImplicitDependencies(projectId);
            case EXPLICIT -> projectService.getExplicitDependencies(projectId);
            case DIRECT -> projectService.getDirectDependencies(projectId);
            default -> throw new IllegalArgumentException(
                    "Invalid dependencyType. Allowed values: DIRECT, FULL, IMPLICIT, EXPLICIT");
        };

        if (StringUtils.hasText(query)) {
            String lowerQuery = query.toLowerCase(Locale.ROOT);
            Set<DependencyDto> filtered = result.getDependencies().stream()
                    .filter(dep -> (dep.getPackageName() != null && dep.getPackageName().toLowerCase(Locale.ROOT).contains(lowerQuery))
                            || (dep.getArtifactName() != null && dep.getArtifactName().toLowerCase(Locale.ROOT).contains(lowerQuery)))
                    .collect(Collectors.toSet());
            result.setDependencies(filtered);
        }

        return result;
    }

    @McpTool(name = "get_project_topology", description = "Get the topology connections map showing app-to-app communication links (e.g., ordering -> customer). Connections are inferred from API-library naming conventions.")
    @Transactional(readOnly = true)
    public Map<String, List<String>> getProjectTopology(
            @McpToolParam(description = "ID of the project", required = true) int projectId) {
        ProjectDto project = projectService.get(projectId);
        return project.getConnections();
    }

    @McpTool(name = "get_project_cyclic_dependencies", description = "Get detected cyclic dependency paths among libraries in the project. Each cycle is a list of application names forming a circular dependency.")
    @Transactional(readOnly = true)
    public Set<List<String>> getProjectCyclicDependencies(
            @McpToolParam(description = "ID of the project", required = true) int projectId) {
        ProjectDto project = projectService.get(projectId);
        Set<List<String>> cycles = project.getLibrariesCycle();
        return cycles != null ? cycles : Set.of();
    }

    @McpTool(name = "find_application_neighbors", description = "Find all applications directly connected to a given application via topology connections or dependency edges (both directions)")
    @Transactional(readOnly = true)
    public List<String> findApplicationNeighbors(
            @McpToolParam(description = "ID of the project", required = true) int projectId,
            @McpToolParam(description = "Name of the application to find neighbors for", required = true) String applicationName) {
        ProjectDto project = projectService.get(projectId);
        Set<String> neighbors = new HashSet<>();

        addNeighborsFromMap(project.getConnections(), applicationName, neighbors);
        addNeighborsFromMap(project.getDependencies(), applicationName, neighbors);

        neighbors.remove(applicationName);
        return neighbors.stream().sorted().collect(Collectors.toList());
    }

    private void addNeighborsFromMap(Map<String, List<String>> map, String appName, Set<String> neighbors) {
        if (map == null) return;
        List<String> outgoing = map.get(appName);
        if (outgoing != null) {
            neighbors.addAll(outgoing);
        }
        map.forEach((from, toList) -> {
            if (toList != null && toList.contains(appName)) {
                neighbors.add(from);
            }
        });
    }

    @McpTool(name = "find_application_dependency_tree", description = "Find the full transitive dependency closure for a given application, recursively resolving all dependencies within the project. Returns a sorted flat list of application names.")
    @Transactional(readOnly = true)
    public List<String> findApplicationDependencyTree(
            @McpToolParam(description = "ID of the project", required = true) int projectId,
            @McpToolParam(description = "Name of the application to resolve the dependency tree for", required = true) String applicationName) {
        ProjectDto project = projectService.get(projectId);
        Map<String, List<String>> dependencies = project.getDependencies();
        Set<String> visited = new HashSet<>();
        collectDependenciesRecursive(applicationName, dependencies, visited);
        visited.remove(applicationName);
        return visited.stream().sorted().collect(Collectors.toList());
    }

    private void collectDependenciesRecursive(String appName, Map<String, List<String>> dependencies, Set<String> visited) {
        if (dependencies == null || visited.contains(appName)) return;
        visited.add(appName);
        List<String> deps = dependencies.get(appName);
        if (deps != null) {
            for (String dep : deps) {
                collectDependenciesRecursive(dep, dependencies, visited);
            }
        }
    }

    @McpTool(name = "filter_applications", description = "Filter applications in a project by any combination of: name (contains search), type (MICROSERVICE/LIBRARY/BOM), owner name, tag name, and tag value. All filters are combined with AND logic.")
    @Transactional(readOnly = true)
    public List<ApplicationLiteDto> filterApplications(
            @McpToolParam(description = "ID of the project", required = true) int projectId,
            @McpToolParam(description = "Filter by application name (case-insensitive contains match)", required = false) String name,
            @McpToolParam(description = "Filter by application type: MICROSERVICE, LIBRARY, or BOM", required = false) String type,
            @McpToolParam(description = "Filter by owner name (matches project-level owners or per-application owners)", required = false) String ownerName,
            @McpToolParam(description = "Filter by tag key (e.g., 'Spring Boot')", required = false) String tagName,
            @McpToolParam(description = "Filter by tag value (e.g., '3.2.0'). Requires tagName to be set.", required = false) String tagValue) {

        ProjectDto project = projectService.get(projectId);
        List<ApplicationLiteDto> apps = project.getApplications();

        Set<String> ownerAppNames = null;
        if (StringUtils.hasText(ownerName) && project.getOwners() != null) {
            ownerAppNames = project.getOwners().stream()
                    .filter(o -> ownerName.equalsIgnoreCase(o.getName()))
                    .flatMap(o -> o.getApplicationNames().stream())
                    .collect(Collectors.toSet());
        }

        final Set<String> finalOwnerAppNames = ownerAppNames;
        final String lowerName = StringUtils.hasText(name) ? name.toLowerCase(Locale.ROOT) : null;

        return apps.stream()
                .filter(app -> {
                    if (lowerName != null && (app.getName() == null || !app.getName().toLowerCase(Locale.ROOT).contains(lowerName))) {
                        return false;
                    }
                    if (StringUtils.hasText(type)) {
                        ApplicationType filterType = parseApplicationType(type);
                        if (filterType == null || app.getType() != filterType) return false;
                    }
                    if (StringUtils.hasText(ownerName)) {
                        boolean matchesProjectOwner = finalOwnerAppNames != null && finalOwnerAppNames.contains(app.getName());
                        boolean matchesAppOwner = app.getOwners() != null && app.getOwners().stream()
                                .anyMatch(owner -> owner.equalsIgnoreCase(ownerName));
                        if (!matchesProjectOwner && !matchesAppOwner) return false;
                    }
                    if (StringUtils.hasText(tagName)) {
                        if (app.getTags() == null) return false;
                        String tagVal = app.getTags().get(tagName);
                        if (tagVal == null) return false;
                        if (StringUtils.hasText(tagValue) && !tagVal.equalsIgnoreCase(tagValue)) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    private ApplicationType parseApplicationType(String type) {
        String normalized = type.trim();
        for (ApplicationType at : ApplicationType.values()) {
            if (at.name().equalsIgnoreCase(normalized) || at.getValue().equalsIgnoreCase(normalized)) {
                return at;
            }
        }
        return null;
    }

    @McpTool(name = "find_dependency_usage", description = "Find which applications use a specific dependency by packageName and/or artifactName. Returns matching dependencies with their usageOf lists. By default searches declared (DIRECT) dependencies; use dependencyType to search FULL/IMPLICIT/EXPLICIT.")
    public List<DependencyDto> findDependencyUsage(
            @McpToolParam(description = "ID of the project", required = true) int projectId,
            @McpToolParam(description = "Filter by package name (group ID), case-insensitive contains match", required = false) String packageName,
            @McpToolParam(description = "Filter by artifact name, case-insensitive contains match", required = false) String artifactName,
            @McpToolParam(description = "Dependency type to search: DIRECT (default), FULL, IMPLICIT, EXPLICIT", required = false) String dependencyType) {

        if (!StringUtils.hasText(packageName) && !StringUtils.hasText(artifactName)) {
            throw new IllegalArgumentException("At least one of 'packageName' or 'artifactName' must be provided");
        }

        ProjectDependenciesDto allDeps = getProjectDependencies(projectId, null, dependencyType);

        final String lowerPackageName = StringUtils.hasText(packageName) ? packageName.toLowerCase(Locale.ROOT) : null;
        final String lowerArtifactName = StringUtils.hasText(artifactName) ? artifactName.toLowerCase(Locale.ROOT) : null;

        return allDeps.getDependencies().stream()
                .filter(dep -> {
                    boolean matches = true;
                    if (lowerPackageName != null) {
                        matches = dep.getPackageName() != null && dep.getPackageName().toLowerCase(Locale.ROOT).contains(lowerPackageName);
                    }
                    if (matches && lowerArtifactName != null) {
                        matches = dep.getArtifactName() != null && dep.getArtifactName().toLowerCase(Locale.ROOT).contains(lowerArtifactName);
                    }
                    return matches;
                })
                .collect(Collectors.toList());
    }

    @McpTool(name = "list_configured_tags", description = "List all pre-configured dependency tag names from application properties. These tags are automatically applied to applications based on their dependencies (e.g., 'Spring Boot', 'MVC', 'RabbitMQ').")
    public Collection<String> listConfiguredTags() {
        return properties.getTags().values().stream().distinct().sorted().collect(Collectors.toList());
    }
}
