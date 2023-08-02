package com.github.rbaul.microservice_visualization.service;

import com.github.rbaul.microservice_visualization.domain.model.LoaderDetails;
import com.github.rbaul.microservice_visualization.domain.model.Project;
import com.github.rbaul.microservice_visualization.domain.model.ProjectVersion;
import com.github.rbaul.microservice_visualization.domain.repository.ProjectRepository;
import com.github.rbaul.microservice_visualization.domain.repository.ProjectVersionRepository;
import com.github.rbaul.microservice_visualization.exception.MicroserviceVisualizationException;
import com.github.rbaul.microservice_visualization.service.loaders.ProjectLoaderFactory;
import com.github.rbaul.microservice_visualization.service.loaders.ProjectNotificationType;
import com.github.rbaul.microservice_visualization.web.dto.ProjectDto;
import com.github.rbaul.microservice_visualization.web.dto.ProjectLiteDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.ArrayList;

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


}
