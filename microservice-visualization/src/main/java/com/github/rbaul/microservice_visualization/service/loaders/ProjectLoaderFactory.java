package com.github.rbaul.microservice_visualization.service.loaders;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class ProjectLoaderFactory {
    private final Map<ProjectLoaderType, ProjectLoaderService> projectLoaderServiceMap = new HashMap<>();

    public ProjectLoaderFactory(List<ProjectLoaderService> projectLoaderServices) {
        projectLoaderServices.forEach(projectLoaderService -> projectLoaderServiceMap.put(projectLoaderService.getType(), projectLoaderService));
    }

    public Optional<ProjectLoaderService> getByType(ProjectLoaderType type) {
        return Optional.ofNullable(projectLoaderServiceMap.get(type));
    }

}
