package com.github.rbaul.microservice_visualization.service.loaders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rbaul.microservice_visualization.config.MicroserviceVisualizationProperties;
import com.github.rbaul.microservice_visualization.domain.model.Application;
import com.github.rbaul.microservice_visualization.domain.model.ApplicationGroup;
import com.github.rbaul.microservice_visualization.domain.model.Owner;
import com.github.rbaul.microservice_visualization.domain.model.Project;
import com.github.rbaul.microservice_visualization.domain.model.ProjectVersion;
import com.github.rbaul.microservice_visualization.utils.ConverterUtils;
import com.github.rbaul.microservice_visualization.utils.Dependency;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public abstract class ProjectLoaderService {
	
	protected final MicroserviceVisualizationProperties properties;
	
	protected final ObjectMapper objectMapper;
	
	public abstract Optional<ProjectVersion> retrieve(MicroserviceVisualizationProperties.MicroserviceVisualizationProject projectProperty);
	
	protected List<ApplicationGroup> getGroups(List<MicroserviceVisualizationProperties.MicroserviceVisualizationGroup> groups) {
		return groups.stream().map(group -> ApplicationGroup.builder()
				.name(group.getName())
				.description(group.getDescription())
				.applicationNames(group.getApplicationNames()).build()).collect(Collectors.toList());
	}
	
	protected List<Owner> getOwners(List<MicroserviceVisualizationProperties.MicroserviceVisualizationOwner> owners) {
		return owners.stream().map(group -> Owner.builder()
				.name(group.getName())
				.description(group.getDescription())
				.applicationNames(group.getApplicationNames()).build()).collect(Collectors.toList());
	}
	
	protected Map<String, List<String>> createTopology(Project project, String applicationPostfix, List<String> applicationApiPostfixes) {
		Map<String, List<String>> appConnections = new HashMap<>();
		Map<String, String> apiNameToOwnerAppName = new HashMap<>();
		
		project.getApplications().forEach(application -> {
			String applicationNameWithoutPostfix = application.getName();
			if (StringUtils.hasText(applicationPostfix)) {
				applicationNameWithoutPostfix = applicationNameWithoutPostfix.substring(0, applicationNameWithoutPostfix.length() - applicationPostfix.length());
			}
			
			for (String applicationApiPostfix : applicationApiPostfixes) {
				apiNameToOwnerAppName.put(applicationNameWithoutPostfix + applicationApiPostfix, application.getName());
			}
			
		});
		
		project.getApplications().forEach(application -> application.getDependencies().forEach(dep -> {
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
	
	protected Application convertApplicationDependencyToApplication(ApplicationDependency applicationDependency) {
		Application application = new Application();
		application.setName(applicationDependency.name());
		String description = StringUtils.hasText(applicationDependency.description()) ?
				applicationDependency.description() : String.format("%s application", applicationDependency.name());
		application.setDescription(description);
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
		return application;
	}
	
	/**
	 * Find application relevant tags
	 */
	private Map<String, String> findTags(List<String> dependencies, Map<String, String> relevantTags) {
		Map<String, String> tags = new HashMap<>();
		dependencies.forEach(dependencyString -> {
			Dependency dependency = ConverterUtils.convertDependency(dependencyString);
			String dependencyName = dependency.packageId() + dependency.name();
			if (relevantTags.containsKey(dependencyName)) {
				tags.put(relevantTags.get(dependencyName), dependency.version());
			}
		});
		return tags;
	}
	
	public abstract Optional<Project> retrieveProject(MicroserviceVisualizationProperties.MicroserviceVisualizationProject projectProperty);
	
	public abstract ProjectLoaderType getType();
}
