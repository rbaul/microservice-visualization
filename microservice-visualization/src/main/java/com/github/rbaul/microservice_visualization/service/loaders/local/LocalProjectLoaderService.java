package com.github.rbaul.microservice_visualization.service.loaders.local;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rbaul.microservice_visualization.config.MicroserviceVisualizationProperties;
import com.github.rbaul.microservice_visualization.domain.model.Application;
import com.github.rbaul.microservice_visualization.domain.model.ApplicationType;
import com.github.rbaul.microservice_visualization.domain.model.LoaderDetails;
import com.github.rbaul.microservice_visualization.domain.model.Project;
import com.github.rbaul.microservice_visualization.service.loaders.ProjectLoaderService;
import com.github.rbaul.microservice_visualization.service.loaders.ProjectLoaderType;
import com.github.rbaul.microservice_visualization.service.model.ProjectConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LocalProjectLoaderService extends ProjectLoaderService {

    public LocalProjectLoaderService(MicroserviceVisualizationProperties properties, ObjectMapper objectMapper) {
        super(properties, objectMapper);
    }

    @Override
    public List<Project> retrieveProject(MicroserviceVisualizationProperties.ProjectDetails projectProperty) {
        String pathLocation = projectProperty.getFolder();

        File[] folderVersions = new File(pathLocation).listFiles(File::isDirectory);
        return folderVersions == null ? List.of() : Arrays.stream(folderVersions).map(dir -> {
            String version = dir.getName();
            return getProjectVersion(LoaderDetails.builder().folder(pathLocation).build(), version, version);
        }).collect(Collectors.toList());
    }

    @Override
    public Project getProjectVersion(LoaderDetails loaderDetails, String versionId, String version) {
        Project project = new Project();
        String dir = MessageFormat.format("{0}/{1}", loaderDetails.getFolder(), versionId);
        ProjectConfig projectConfiguration = getProjectConfiguration(Paths.get(MessageFormat.format("{0}/{1}", dir, PROJECT_CONFIG_YAML)));
        List<Application> applicationDependencies = getApplicationsByType(dir, APPLICATIONS_FOLDER, ApplicationType.MICROSERVICE);
        List<Application> libraryDependencies = getApplicationsByType(dir, LIBRARIES_FOLDER, ApplicationType.LIBRARY);
        List<Application> bomDependencies = getApplicationsByType(dir, BOMS_FOLDER, ApplicationType.BOM);
        List<Application> dependencies = new ArrayList<>(applicationDependencies);
        dependencies.addAll(libraryDependencies);
        dependencies.addAll(bomDependencies);

        setApplicationToProject(project, projectConfiguration, dependencies, versionId, version);
        return project;
    }

    private List<Application> getApplicationsByType(String dir, String applicationsFolder, ApplicationType defaultType) {
        Path folder = Paths.get(MessageFormat.format("{0}/{1}", dir, applicationsFolder));

        File[] listApplicationFiles = folder.toFile().listFiles(File::isFile);
        return listApplicationFiles == null ? List.of() : Arrays.stream(listApplicationFiles)
                .map(file -> readContent(Paths.get(file.toURI()))
                        .flatMap(contentString -> convertContentToApplication(contentString, defaultType)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Override
    protected String getFullName(MicroserviceVisualizationProperties.ProjectDetails projectProperty) {
        return projectProperty.getFolder();
    }

    @Override
    public ProjectLoaderType getType() {
        return ProjectLoaderType.local;
    }

    protected Optional<String> readContent(Path path) {
        try {
            return Optional.of(Files.readString(path));
        } catch (IOException e) {
            log.error("Failed read application file {}", path, e);
            return Optional.empty();
        }
    }

    protected ProjectConfig getProjectConfiguration(Path path) {
        try {
            return getProjectConfig(Files.readString(path));
        } catch (IOException e) {
            log.error("Failed read application file {}", path, e);
            return null;
        }
    }

}
