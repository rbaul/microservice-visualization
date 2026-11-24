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
import com.github.rbaul.microservice_visualization.web.dto.ProjectVersionDto;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedModel;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class McpToolService {

	private final ProjectVersionService projectVersionService;
	private final ProjectService projectService;
	private final ApplicationService applicationService;
	private final ProjectVersionRepository projectVersionRepository;
	private final MicroserviceVisualizationProperties properties;

	// --- Tool 1: list_project_versions ---
	@McpTool(name = "list_project_versions", description = "List all available project versions with optional text filter and pagination")
	public PagedModel<ProjectVersionDto> listProjectVersions(
			@McpToolParam(description = "Optional text filter for project version names") String filter,
			@McpToolParam(description = "Page number (0-based), defaults to 0") Integer page,
			@McpToolParam(description = "Page size, defaults to 20") Integer size) {
		int pageNum = page != null ? page : 0;
		int pageSize = size != null ? size : 20;
		return new PagedModel<>(projectVersionService.search(filter, PageRequest.of(pageNum, pageSize)));
	}

	// --- Tool 2: get_project_version ---
	@McpTool(name = "get_project_version", description = "Get a project version by its numeric ID or display name. Returns the project version with its list of project snapshots (branches/versions).")
	@Transactional(readOnly = true)
	public ProjectVersionDto getProjectVersion(
			@McpToolParam(description = "Numeric ID of the project version") Integer id,
			@McpToolParam(description = "Display name of the project version (e.g., 'Demo Github project')") String name) {
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

	// --- Tool 3: list_projects ---
	@McpTool(name = "list_projects", description = "List project snapshots (branches/versions) for a given project version, with pagination")
	public PagedModel<?> listProjects(
			@McpToolParam(description = "ID of the project version", required = true) int projectVersionId,
			@McpToolParam(description = "Page number (0-based), defaults to 0") Integer page,
			@McpToolParam(description = "Page size, defaults to 20") Integer size) {
		int pageNum = page != null ? page : 0;
		int pageSize = size != null ? size : 20;
		return new PagedModel<>(projectService.search(projectVersionId, PageRequest.of(pageNum, pageSize)));
	}

	// --- Tool 4: get_project ---
	@McpTool(name = "get_project", description = "Get full project details including applications, topology connections, dependency map, cyclic dependencies, groups, owners, and tags")
	public ProjectDto getProject(
			@McpToolParam(description = "ID of the project", required = true) int projectId) {
		return projectService.get(projectId);
	}

	// --- Tool 5: get_application ---
	@McpTool(name = "get_application", description = "Get full application details including its direct dependencies and dependency management (BOM) information")
	public ApplicationFullDto getApplication(
			@McpToolParam(description = "ID of the application", required = true) long applicationId) {
		return applicationService.get(applicationId);
	}

	// --- Tool 6: get_project_dependencies ---
	@McpTool(name = "get_project_dependencies", description = "Get project dependencies with optional keyword search and dependency type filter. Each dependency includes which applications use it (usageOf).")
	public ProjectDependenciesDto getProjectDependencies(
			@McpToolParam(description = "ID of the project", required = true) int projectId,
			@McpToolParam(description = "Optional search keyword to filter by packageName or artifactName (case-insensitive contains match)") String query,
			@McpToolParam(description = "Dependency type: DIRECT (declared dependencies, default), FULL (all resolved), IMPLICIT (not BOM-managed), EXPLICIT (BOM-managed only)") String dependencyType) {

		String type = StringUtils.hasText(dependencyType) ? dependencyType.toUpperCase() : "DIRECT";
		ProjectDependenciesDto result = switch (type) {
			case "FULL" -> projectService.getDependencies(projectId);
			case "IMPLICIT" -> projectService.getImplicitDependencies(projectId);
			case "EXPLICIT" -> projectService.getExplicitDependencies(projectId);
			default -> projectService.getDirectDependencies(projectId);
		};

		if (StringUtils.hasText(query)) {
			String lowerQuery = query.toLowerCase();
			Set<DependencyDto> filtered = result.getDependencies().stream()
					.filter(dep -> (dep.getPackageName() != null && dep.getPackageName().toLowerCase().contains(lowerQuery))
							|| (dep.getArtifactName() != null && dep.getArtifactName().toLowerCase().contains(lowerQuery)))
					.collect(Collectors.toSet());
			result.setDependencies(filtered);
		}

		return result;
	}

	// --- Tool 7: get_project_topology ---
	@McpTool(name = "get_project_topology", description = "Get the topology connections map showing app-to-app communication links (e.g., ordering -> customer). Connections are inferred from API-library naming conventions.")
	@Transactional(readOnly = true)
	public Map<String, List<String>> getProjectTopology(
			@McpToolParam(description = "ID of the project", required = true) int projectId) {
		ProjectDto project = projectService.get(projectId);
		return project.getConnections();
	}

	// --- Tool 8: get_project_cyclic_dependencies ---
	@McpTool(name = "get_project_cyclic_dependencies", description = "Get detected cyclic dependency paths among libraries in the project. Each cycle is a list of application names forming a circular dependency.")
	@Transactional(readOnly = true)
	public Set<List<String>> getProjectCyclicDependencies(
			@McpToolParam(description = "ID of the project", required = true) int projectId) {
		ProjectDto project = projectService.get(projectId);
		Set<List<String>> cycles = project.getLibrariesCycle();
		return cycles != null ? cycles : Set.of();
	}

	// --- Tool 9: find_application_neighbors ---
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
		return new ArrayList<>(neighbors);
	}

	private void addNeighborsFromMap(Map<String, List<String>> map, String appName, Set<String> neighbors) {
		if (map == null) return;
		// Outgoing edges
		List<String> outgoing = map.get(appName);
		if (outgoing != null) {
			neighbors.addAll(outgoing);
		}
		// Incoming edges
		map.forEach((from, toList) -> {
			if (toList != null && toList.contains(appName)) {
				neighbors.add(from);
			}
		});
	}

	// --- Tool 10: find_application_dependency_tree ---
	@McpTool(name = "find_application_dependency_tree", description = "Find the full transitive dependency tree for a given application, recursively resolving all dependencies within the project")
	@Transactional(readOnly = true)
	public List<String> findApplicationDependencyTree(
			@McpToolParam(description = "ID of the project", required = true) int projectId,
			@McpToolParam(description = "Name of the application to resolve the dependency tree for", required = true) String applicationName) {
		ProjectDto project = projectService.get(projectId);
		Map<String, List<String>> dependencies = project.getDependencies();
		Set<String> visited = new HashSet<>();
		collectDependenciesRecursive(applicationName, dependencies, visited);
		visited.remove(applicationName);
		return new ArrayList<>(visited);
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

	// --- Tool 11: filter_applications ---
	@McpTool(name = "filter_applications", description = "Filter applications in a project by any combination of: name (contains search), type (MICROSERVICE/LIBRARY/BOM), owner name, tag name, and tag value. All filters are combined with AND logic.")
	@Transactional(readOnly = true)
	public List<ApplicationLiteDto> filterApplications(
			@McpToolParam(description = "ID of the project", required = true) int projectId,
			@McpToolParam(description = "Filter by application name (case-insensitive contains match)") String name,
			@McpToolParam(description = "Filter by application type: MICROSERVICE, LIBRARY, or BOM") String type,
			@McpToolParam(description = "Filter by owner name") String ownerName,
			@McpToolParam(description = "Filter by tag key (e.g., 'Spring Boot')") String tagName,
			@McpToolParam(description = "Filter by tag value (e.g., '3.2.0'). Requires tagName to be set.") String tagValue) {

		ProjectDto project = projectService.get(projectId);
		List<ApplicationLiteDto> apps = project.getApplications();

		// Resolve owner -> app names mapping
		Set<String> ownerAppNames = null;
		if (StringUtils.hasText(ownerName) && project.getOwners() != null) {
			ownerAppNames = project.getOwners().stream()
					.filter(o -> ownerName.equalsIgnoreCase(o.getName()))
					.flatMap(o -> o.getApplicationNames().stream())
					.collect(Collectors.toSet());
		}

		final Set<String> finalOwnerAppNames = ownerAppNames;

		return apps.stream()
				.filter(app -> {
					if (StringUtils.hasText(name) && (app.getName() == null || !app.getName().toLowerCase().contains(name.toLowerCase()))) {
						return false;
					}
					if (StringUtils.hasText(type)) {
						try {
							ApplicationType filterType = ApplicationType.valueOf(type.toUpperCase());
							if (app.getType() != filterType) return false;
						} catch (IllegalArgumentException e) {
							return false;
						}
					}
					if (finalOwnerAppNames != null && !finalOwnerAppNames.contains(app.getName())) {
						return false;
					}
					if (StringUtils.hasText(tagName) && app.getTags() != null) {
						String tagVal = app.getTags().get(tagName);
						if (tagVal == null) return false;
						if (StringUtils.hasText(tagValue) && !tagVal.equalsIgnoreCase(tagValue)) return false;
					} else if (StringUtils.hasText(tagName)) {
						return false;
					}
					return true;
				})
				.collect(Collectors.toList());
	}

	// --- Tool 12: find_dependency_usage ---
	@McpTool(name = "find_dependency_usage", description = "Find which applications use a specific dependency by packageName and/or artifactName. Returns matching dependencies with their usageOf lists.")
	public List<DependencyDto> findDependencyUsage(
			@McpToolParam(description = "ID of the project", required = true) int projectId,
			@McpToolParam(description = "Filter by package name (group ID), case-insensitive contains match") String packageName,
			@McpToolParam(description = "Filter by artifact name, case-insensitive contains match") String artifactName) {

		if (!StringUtils.hasText(packageName) && !StringUtils.hasText(artifactName)) {
			throw new IllegalArgumentException("At least one of 'packageName' or 'artifactName' must be provided");
		}

		ProjectDependenciesDto allDeps = projectService.getDirectDependencies(projectId);

		return allDeps.getDependencies().stream()
				.filter(dep -> {
					boolean matches = true;
					if (StringUtils.hasText(packageName)) {
						matches = dep.getPackageName() != null && dep.getPackageName().toLowerCase().contains(packageName.toLowerCase());
					}
					if (matches && StringUtils.hasText(artifactName)) {
						matches = dep.getArtifactName() != null && dep.getArtifactName().toLowerCase().contains(artifactName.toLowerCase());
					}
					return matches;
				})
				.collect(Collectors.toList());
	}

	// --- Tool 13: list_configured_tags ---
	@McpTool(name = "list_configured_tags", description = "List all pre-configured dependency tag names from application properties. These tags are automatically applied to applications based on their dependencies (e.g., 'Spring Boot', 'MVC', 'RabbitMQ').")
	public Collection<String> listConfiguredTags() {
		return properties.getTags().values().stream().distinct().sorted().collect(Collectors.toList());
	}
}
