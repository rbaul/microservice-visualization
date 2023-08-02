package com.github.rbaul.microservice_visualization.config;

import com.github.rbaul.microservice_visualization.service.loaders.ProjectLoaderType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "microservice-visualization")
public class MicroserviceVisualizationProperties {

    /**
     * Relevant Dependency tags
     */
    private Map<String, String> tags = new HashMap<>();

    /**
     * Project loaders
     */
    private Map<ProjectLoaderType, ProjectLoader> loaders = new HashMap<>();

    @Getter
    @Setter
    public static class ProjectDetails {
        private String name;
        private String description;
        private String defaultVersion;
        private String url;
        private String project;
        private String repo;
        private String token;
        private String folder;
    }

    @Getter
    @Setter
    public static class ProjectLoader {
        private Boolean enabled = false;
        private List<ProjectDetails> projects = new ArrayList<>();
    }
}
