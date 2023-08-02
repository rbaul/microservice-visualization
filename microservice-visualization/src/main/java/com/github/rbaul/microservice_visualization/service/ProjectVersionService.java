package com.github.rbaul.microservice_visualization.service;

import com.github.rbaul.microservice_visualization.config.MicroserviceVisualizationProperties;
import com.github.rbaul.microservice_visualization.domain.model.ProjectVersion;
import com.github.rbaul.microservice_visualization.domain.repository.ProjectVersionRepository;
import com.github.rbaul.microservice_visualization.service.loaders.ProjectLoaderFactory;
import com.github.rbaul.microservice_visualization.service.loaders.ProjectLoaderType;
import com.github.rbaul.microservice_visualization.web.dto.ProjectVersionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProjectVersionService {
    private final ProjectVersionRepository projectVersionRepository;

    private final ModelMapper modelMapper;

    private final ProjectLoaderFactory projectLoaderFactory;

    public void loadProjects(MicroserviceVisualizationProperties projectProperty) {
        Map<ProjectLoaderType, MicroserviceVisualizationProperties.ProjectLoader> projectLoaders = projectProperty.getLoaders();
        projectLoaders.forEach((projectLoaderType, projectLoader) -> {
            if (projectLoader.getEnabled()) {
                projectLoaderFactory.getByType(projectLoaderType).ifPresent(projectLoaderService -> projectLoader.getProjects()
                        .forEach(projectDetails -> {
                            log.info("Load project '{} - {}' - Starting...", projectLoaderType, projectDetails.getName());
                            projectLoaderService.retrieve(projectDetails)
                                    .ifPresentOrElse(projectVersion -> {
                                        projectVersionRepository.save(projectVersion);
                                        log.info("Load project '{} - {}' - Start completed.", projectLoaderType, projectDetails.getName());
                                    }, () -> log.error("Not found project loader of {}", projectLoaderType));
                        }));
            }
        });
    }

    @Transactional(readOnly = true)
    public ProjectVersionDto get(int id) {
        return getDto(getById(id));
    }

    private ProjectVersion getById(int id) {
        return projectVersionRepository.findById(id)
                .orElseThrow(() -> new EmptyResultDataAccessException("No found project version with id: " + id, 1));
    }

    @Transactional(readOnly = true)
    public Page<ProjectVersionDto> search(String filter, Pageable pageable) {
        return projectVersionRepository.findAll(pageable).map(this::getDto);
    }

    private ProjectVersionDto getDto(ProjectVersion projectVersion) {
        return modelMapper.map(projectVersion, ProjectVersionDto.class);
    }

    public void delete(int id) {
        projectVersionRepository.deleteById(id);
    }

    public void deleteAll() {
        projectVersionRepository.deleteAll(); // Remove all
    }

}
