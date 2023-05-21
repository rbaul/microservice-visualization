package com.github.rbaul.microservice_visualization.config;

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
	
	private List<MicroserviceVisualizationProject> projects = new ArrayList<>();
	
	@Getter
	@Setter
	public static class MicroserviceVisualizationProject {
		private String name;
		private String description;
		private String pathLocation;
		
		private String applicationPostfix = "";
		
		private List<String> applicationApiPostfixes = List.of("-api");
		
		private List<MicroserviceVisualizationGroup> groups;
		
		private List<MicroserviceVisualizationOwner> owners;
	}
	
	@Getter
	@Setter
	public static class MicroserviceVisualizationGroup {
		private String name;
		private String description;
		private List<String> applicationNames;
	}
	
	@Getter
	@Setter
	public static class MicroserviceVisualizationOwner {
		private String name;
		private String description;
		private List<String> applicationNames;
	}
	
}
