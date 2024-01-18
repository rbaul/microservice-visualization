package com.github.rbaul.microservice_visualization.service.loaders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.rbaul.microservice_visualization.config.MicroserviceVisualizationProperties;
import com.github.rbaul.microservice_visualization.domain.model.*;
import com.github.rbaul.microservice_visualization.service.model.ApplicationDependency;
import com.github.rbaul.microservice_visualization.service.model.ProjectConfig;
import com.github.rbaul.microservice_visualization.utils.ConverterUtils;
import com.github.rbaul.microservice_visualization.utils.Dependency;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public abstract class ProjectLoaderService {

    public static final String APPLICATIONS_FOLDER = "applications";
    public static final String LIBRARIES_FOLDER = "libraries";
    public static final String PROJECT_CONFIG_YAML = "project-config.yaml";

    protected final MicroserviceVisualizationProperties properties;

    protected final ObjectMapper objectMapper;

    protected final ObjectMapper objectMapperYaml = new ObjectMapper(new YAMLFactory());

    public Optional<ProjectVersion> retrieve(MicroserviceVisualizationProperties.ProjectDetails projectProperty) {
        ProjectVersion projectVersion = new ProjectVersion();
        projectVersion.setName(projectProperty.getName());
        projectVersion.setDescription(projectProperty.getDescription());
        List<Project> projects = retrieveProject(projectProperty);
        String defaultVersion = projectProperty.getDefaultVersion();
        projects.forEach(projectVersion::addProject);

        projects.stream().filter(project -> project.getVersion().equals(defaultVersion))
                .findFirst().ifPresentOrElse(projectVersion::setMainProject,
                        () -> projectVersion.setMainProject(projects.get(0)));

        LoaderDetails loaderDetails = LoaderDetails.builder()
                .url(projectProperty.getUrl())
                .repo(projectProperty.getRepo())
                .project(projectProperty.getProject())
                .token(projectProperty.getToken())
                .loaderType(getType())
                .folder(projectProperty.getFolder()).build();
        projectVersion.setLoaderDetails(loaderDetails);

        projectVersion.setFullName(getFullName(projectProperty));

        return Optional.of(projectVersion);
    }

    protected abstract String getFullName(MicroserviceVisualizationProperties.ProjectDetails projectProperty);

    protected List<ApplicationGroup> getGroups(List<ProjectConfig.GroupConfig> groups) {
        return groups.stream().map(group -> ApplicationGroup.builder()
                .name(group.getName())
                .description(group.getDescription())
                .applicationNames(group.getApplicationNames()).build()).collect(Collectors.toList());
    }

    protected List<Owner> getOwners(List<ProjectConfig.OwnerConfig> owners) {
        return owners.stream().map(group -> Owner.builder()
                .name(group.getName())
                .description(group.getDescription())
                .applicationNames(group.getApplicationNames()).build()).collect(Collectors.toList());
    }

    protected Map<String, List<String>> createTopology(Project project, String applicationPostfix, List<String> applicationApiPostfixes) {
        Map<String, List<String>> appConnections = new HashMap<>();
        Map<String, String> apiNameToOwnerAppName = new HashMap<>();

        project.getApplications().stream()
                .filter(application -> application.getType() == ApplicationType.MICROSERVICE).forEach(application -> {
                    String applicationNameWithoutPostfix = application.getName();
                    if (StringUtils.hasText(applicationPostfix)) {
                        applicationNameWithoutPostfix = applicationNameWithoutPostfix.substring(0, applicationNameWithoutPostfix.length() - applicationPostfix.length());
                    }

                    for (String applicationApiPostfix : applicationApiPostfixes) {
                        apiNameToOwnerAppName.put(applicationNameWithoutPostfix + applicationApiPostfix, application.getName());
                    }

                });

        project.getApplications().stream()
                .filter(application -> application.getType() == ApplicationType.MICROSERVICE)
                .forEach(application -> application.getDependencies().forEach(dep -> {
                    Dependency dependency = ConverterUtils.convertDependency(dep);
                    if (apiNameToOwnerAppName.containsKey(dependency.name()) && !apiNameToOwnerAppName.get(dependency.name()).equals(application.getName())) {
                        if (!appConnections.containsKey(application.getName())) {
                            appConnections.put(application.getName(), new ArrayList<>());
                        }
                        appConnections.get(application.getName()).add(apiNameToOwnerAppName.get(dependency.name()));
                    }
                }));

        return appConnections;
    }

    protected Map<String, List<String>> createDependenciesMap(Project project) {
        Map<String, List<String>> appConnections = new HashMap<>();
        Set<String> appNames = project.getApplications().stream().map(Application::getName).collect(Collectors.toSet());

        project.getApplications().forEach(application -> {
            appConnections.put(application.getName(), new ArrayList<>());
            if (application.getDependencies() != null) {
                application.getDependencies().forEach(dep -> {
                    Dependency dependency = ConverterUtils.convertDependency(dep);
                    if (!application.getName().equals(dependency.name()) && appNames.contains(dependency.name())) {
                        appConnections.get(application.getName()).add(dependency.name());
                    }
                });
            }
        });

        return appConnections;
    }

    protected Set<String> createProjectRelevantTags(Project project) {
        Set<String> tags = new HashSet<>();

        project.getApplications().forEach(application -> tags.addAll(application.getTags().keySet()));

        return tags;
    }

    protected Application convertApplicationDependencyToApplication(ApplicationDependency applicationDependency, ApplicationType type) {
        Application application = new Application();
        application.setName(applicationDependency.name());
        String description = StringUtils.hasText(applicationDependency.description()) ?
                applicationDependency.description() : String.format("%s application", applicationDependency.name());
        application.setDescription(description);
        application.setLocation(applicationDependency.location());
        application.setOwner(applicationDependency.owner());
        application.setLabel(applicationDependency.label());
        application.setDependencies(applicationDependency.dependencies());
        application.setManagementDependencies(applicationDependency.managementDependencies());
        Map<String, String> tags = new HashMap<>(applicationDependency.tags());

        Map<String, String> relevantTags = properties.getTags();

        // Management dependencies
        tags.putAll(findTags(applicationDependency.managementDependencies(), relevantTags));

        // Dependencies
        tags.putAll(findTags(applicationDependency.dependencies(), relevantTags));
        application.setTags(tags);
        application.setType(type);
        return application;
    }

    /**
     * Find application relevant tags
     */
    private Map<String, String> findTags(List<String> dependencies, Map<String, String> relevantTags) {
        Map<String, String> tags = new HashMap<>();
        if (dependencies != null) {
            dependencies.forEach(dependencyString -> {
                Dependency dependency = ConverterUtils.convertDependency(dependencyString);
                String dependencyName = dependency.packageId() + dependency.name();
                if (relevantTags.containsKey(dependencyName)) {
                    tags.put(relevantTags.get(dependencyName), dependency.version());
                }
            });
        }
        return tags;
    }

    protected ApplicationDependency getApplicationDependency(String fileContent) {
        try {
            return objectMapper.readValue(fileContent, ApplicationDependency.class);
        } catch (IOException e) {
            log.error("Failed read application file", e);
            return null;
        }
    }

    /**
     * Get Project Configuration from content
     */
    public ProjectConfig getProjectConfig(String fileContent) {
        try {
            return objectMapperYaml.readValue(fileContent, ProjectConfig.class);
        } catch (JsonProcessingException e) {
            log.error("Failed read application file", e);
            return null;
        }
    }

    public abstract List<Project> retrieveProject(MicroserviceVisualizationProperties.ProjectDetails projectProperty);

    protected void setApplicationToProject(Project project, ProjectConfig projectConfig, List<Application> applicationDependencies, String versionId, String version) {
        project.setApplications(applicationDependencies);
        // Connections
        project.setConnections(createTopology(project, projectConfig.getApplicationPostfix(), projectConfig.getApplicationApiPostfixes()));
        // Dependencies
        project.setDependencies(createDependenciesMap(project));
        // Relevant Tags
        project.setTags(createProjectRelevantTags(project));
        if (!CollectionUtils.isEmpty(projectConfig.getGroups())) {
            List<ApplicationGroup> groups = getGroups(projectConfig.getGroups());
            List<ApplicationGroup> filteredGroups = groups.stream()
                    .filter(applicationGroup -> applicationGroup.getApplicationNames().stream()
                            .anyMatch(appName -> applicationDependencies.stream()
                                    .anyMatch(application -> application.getName().equals(appName)))).collect(Collectors.toList());
            project.setGroups(filteredGroups);
        }
        if (!CollectionUtils.isEmpty(projectConfig.getOwners())) {
            List<Owner> owners = getOwners(projectConfig.getOwners());
            List<Owner> filteredOwners = owners.stream()
                    .filter(owner -> owner.getApplicationNames().stream()
                            .anyMatch(appName -> applicationDependencies.stream()
                                    .anyMatch(application -> application.getName().equals(appName)))).collect(Collectors.toList());
            project.setOwners(filteredOwners);
        }
        project.setVersionId(versionId);
        project.setVersion(version);
    }

    public abstract Project getProjectVersion(LoaderDetails loaderDetails, String versionId, String version);

    public abstract ProjectLoaderType getType();
}
