package com.github.rbaul.microservice_visualization.service;

import com.github.rbaul.microservice_visualization.config.MicroserviceVisualizationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class SyncService {

    private final ProjectVersionService projectVersionService;
    private final MicroserviceVisualizationProperties properties;


    /**
     * Sync all project from properties
     */
    @Async
    public void sync() {
        log.info("Load initial projects - Starting...");
        projectVersionService.deleteAll();
        projectVersionService.loadProjects(properties);
        log.info("Load initial projects - Start completed.");
    }
}
