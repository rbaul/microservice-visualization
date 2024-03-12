package com.github.rbaul.microservice_visualization.service;

import com.github.rbaul.microservice_visualization.domain.model.*;
import com.github.rbaul.microservice_visualization.domain.repository.ProjectRepository;
import com.github.rbaul.microservice_visualization.domain.repository.ProjectVersionRepository;
import com.github.rbaul.microservice_visualization.exception.MicroserviceVisualizationException;
import com.github.rbaul.microservice_visualization.service.loaders.ProjectLoaderFactory;
import com.github.rbaul.microservice_visualization.utils.ConverterUtils;
import com.github.rbaul.microservice_visualization.utils.Dependency;
import com.github.rbaul.microservice_visualization.web.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProjectService {
    private final ProjectRepository projectRepository;

    private final ProjectVersionRepository projectVersionRepository;

    private final ModelMapper modelMapper;

    private final ProjectLoaderFactory projectLoaderFactory;

    @Transactional(readOnly = true)
    public ProjectDto get(int id) {
        return getDto(getById(id));
    }

    private Project getById(int id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new EmptyResultDataAccessException("No found project with id: " + id, 1));
    }

    public ProjectDto create(ProjectDto projectDto) {
        return null; // TODO: Support crate project on the fly
    }

    @Transactional
    public ProjectDto create(String fullName, String version) {
        ProjectVersion projectVersion = projectVersionRepository.findByFullName(fullName).orElseThrow(() -> new MicroserviceVisualizationException(MessageFormat.format("Project Version no exist with id {0}", fullName)));
        LoaderDetails loaderDetails = projectVersion.getLoaderDetails();
        projectLoaderFactory.getByType(loaderDetails.getLoaderType())
                .ifPresent(projectLoaderService -> {
                    Project projectData = projectLoaderService.getProjectVersion(loaderDetails, version, version);
                    projectVersion.addProject(projectData);
                });

        return null;
    }

    public void delete(int id) {
        projectRepository.deleteById(id);
    }

    public void delete(String fullName, String version) {
        Project project = projectRepository.findByProjectVersionFullNameAndVersion(fullName, version)
                .orElseThrow(() -> new MicroserviceVisualizationException(MessageFormat.format("Project no exist with name {0} and version {1}", fullName, version)));
        projectRepository.delete(project);
    }

    private ProjectDto getDto(Project project) {
        return modelMapper.map(project, ProjectDto.class);
    }

    @Transactional
    public void mergeProjects(int projectAId, int projectBId) {
        Project projectA = getById(projectAId);
        Project projectB = getById(projectBId);
        // TODO: Merge
    }

    public Page<ProjectLiteDto> search(int projectVersionId, Pageable pageable) {
        return projectRepository.findByProjectVersionId(projectVersionId, pageable)
                .map(project -> modelMapper.map(project, ProjectLiteDto.class));
    }

    @Transactional
    public void update(String fullName, String version) {
        Project project = projectRepository.findByProjectVersionFullNameAndVersion(fullName, version)
                .orElseThrow(() -> new MicroserviceVisualizationException(MessageFormat.format("Project no exist with name {0} and version {1}", fullName, version)));
        ProjectVersion projectVersion = project.getProjectVersion();
        LoaderDetails loaderDetails = projectVersion.getLoaderDetails();

        projectLoaderFactory.getByType(loaderDetails.getLoaderType())
                .ifPresent(projectLoaderService -> {
                    Project projectData = projectLoaderService.getProjectVersion(loaderDetails, project.getVersionId(), project.getVersion());
                    project.setApplications(new ArrayList<>(projectData.getApplications()));
                    project.setConnections(projectData.getConnections());
                    project.setGroups(projectData.getGroups());
                    project.setOwners(projectData.getOwners());
                });
    }


    @Transactional(readOnly = true)
    public ProjectDependenciesDto getDependencies(int id) {
        Project project = getById(id);

        Map<String, DependencyDto> dependencies = new HashMap<>();
        Set<Application> applications = project.getApplications();
        for (Application application : applications) {
            if (application.getType() != ApplicationType.BOM) {
                for (DependencyEntity dependency : application.getFullDependencies()) {
                    String dependencyString = ConverterUtils.getDependencyFormatted(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
                    if (!dependencies.containsKey(dependencyString)) {
                        dependencies.put(dependencyString, DependencyDto.builder()
                                .packageName(dependency.getGroupId())
                                .artifactName(dependency.getArtifactId())
                                .version(dependency.getVersion()).build());
                    }
                    dependencies.get(dependencyString).getUsageOf().add(application.getName());
                }
            }
        }

        ProjectVersion projectVersion = project.getProjectVersion();
        ProjectVersionLiteDto projectVersionLiteDto = modelMapper.map(projectVersion, ProjectVersionLiteDto.class);
        return ProjectDependenciesDto.builder()
                .id(project.getId())
                .version(project.getVersion())
                .dependencies(new HashSet<>(dependencies.values()))
                .projectVersion(projectVersionLiteDto).build();
    }

    @Transactional(readOnly = true)
    public ProjectDependenciesDto getImplicitDependencies(int id) {
        Project project = getById(id);

        Map<String, DependencyDto> dependencies = new HashMap<>();
        Set<Application> applications = project.getApplications();
        for (Application application : applications) {
            if (application.getType() != ApplicationType.BOM) {
                for (String dependencyString : application.getDependencies()) {

                    Set<String> dependencyManagements = application.getDependencyManagements().stream()
                            .map(DependencyManagement::getDependencies)
                            .flatMap(List::stream).collect(Collectors.toSet());

                    if (!dependencyManagements.contains(dependencyString)) {
                        Dependency dependency = ConverterUtils.convertDependency(dependencyString);

                        if (!dependencies.containsKey(dependencyString)) {
                            dependencies.put(dependencyString, DependencyDto.builder()
                                    .packageName(dependency.packageId())
                                    .artifactName(dependency.name())
                                    .version(dependency.version()).build());
                        }
                        dependencies.get(dependencyString).getUsageOf().add(application.getName());
                    }
                }
            }
        }

        ProjectVersion projectVersion = project.getProjectVersion();
        ProjectVersionLiteDto projectVersionLiteDto = modelMapper.map(projectVersion, ProjectVersionLiteDto.class);
        return ProjectDependenciesDto.builder()
                .id(project.getId())
                .version(project.getVersion())
                .dependencies(new HashSet<>(dependencies.values()))
                .projectVersion(projectVersionLiteDto).build();
    }

    @Transactional(readOnly = true)
    public ProjectDependenciesDto getDirectDependencies(int id) {
        Project project = getById(id);

        Map<String, DependencyDto> dependencies = new HashMap<>();
        Set<Application> applications = project.getApplications();
        for (Application application : applications) {
            if (application.getType() != ApplicationType.BOM) {
                for (String dependencyString : application.getDependencies()) {

                    Dependency dependency = ConverterUtils.convertDependency(dependencyString);

                    if (!dependencies.containsKey(dependencyString)) {
                        dependencies.put(dependencyString, DependencyDto.builder()
                                .packageName(dependency.packageId())
                                .artifactName(dependency.name())
                                .version(dependency.version()).build());
                    }
                    dependencies.get(dependencyString).getUsageOf().add(application.getName());
                }
            }
        }

        ProjectVersion projectVersion = project.getProjectVersion();
        ProjectVersionLiteDto projectVersionLiteDto = modelMapper.map(projectVersion, ProjectVersionLiteDto.class);
        return ProjectDependenciesDto.builder()
                .id(project.getId())
                .version(project.getVersion())
                .dependencies(new HashSet<>(dependencies.values()))
                .projectVersion(projectVersionLiteDto).build();
    }
}
