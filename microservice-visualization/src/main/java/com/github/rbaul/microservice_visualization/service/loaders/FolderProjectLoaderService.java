package com.github.rbaul.microservice_visualization.service.loaders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rbaul.microservice_visualization.config.MicroserviceVisualizationProperties;
import com.github.rbaul.microservice_visualization.domain.model.Application;
import com.github.rbaul.microservice_visualization.domain.model.Project;
import com.github.rbaul.microservice_visualization.domain.model.ProjectVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class FolderProjectLoaderService extends ProjectLoaderService {
	
	public static final String DEFAULT_VERSION = "1.0.0";
	
	public FolderProjectLoaderService(MicroserviceVisualizationProperties properties, ObjectMapper objectMapper) {
		super(properties, objectMapper);
	}
	
	@Override
	public Optional<ProjectVersion> retrieve(MicroserviceVisualizationProperties.MicroserviceVisualizationProject projectProperty) {
		ProjectVersion projectVersion = new ProjectVersion();
		projectVersion.setName(projectProperty.getName());
		projectVersion.setDescription(projectProperty.getDescription());
		Optional<Project> projectOptional = retrieveProject(projectProperty);
		projectOptional.ifPresent(project -> {
			projectVersion.setMainProject(project);
			projectVersion.addProject(project);
		});
		
		return Optional.of(projectVersion);
	}
	
	@Override
	public Optional<Project> retrieveProject(MicroserviceVisualizationProperties.MicroserviceVisualizationProject projectProperty) {
		String pathLocation = projectProperty.getPathLocation();
		Path folder = Paths.get(pathLocation);
		try (Stream<Path> paths = Files.walk(folder)) {
			Project project = new Project();
			List<Application> applicationDependencies = paths.filter(Files::isRegularFile)
					.map(this::getApplication)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			project.setApplications(applicationDependencies);
			project.setConnections(createTopology(project, projectProperty.getApplicationPostfix(), projectProperty.getApplicationApiPostfix()));
			project.setGroups(getGroups(projectProperty.getGroups()));
			project.setVersion(DEFAULT_VERSION);
			return Optional.of(project);
		} catch (IOException e) {
			log.error("Failed load projects from {}", pathLocation, e);
			return Optional.empty();
		}
	}
	
	@Override
	public ProjectLoaderType getType() {
		return ProjectLoaderType.FOLDER;
	}
	
	protected ApplicationDependency getApplicationDependency(Path path) {
		try {
			return objectMapper.readValue(path.toFile(), ApplicationDependency.class);
		} catch (IOException e) {
			log.error("Failed read application file {}", path, e);
			return null;
		}
	}
	
	protected Application getApplication(Path path) {
		ApplicationDependency applicationDependency = getApplicationDependency(path);
		if (applicationDependency != null) {
			return convertApplicationDependencyToApplication(applicationDependency);
		}
		return null;
	}
}
