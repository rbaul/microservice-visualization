package com.github.rbaul.microservice_visualization.init;

import com.github.rbaul.microservice_visualization.config.MicroserviceVisualizationProperties;
import com.github.rbaul.microservice_visualization.service.ProjectVersionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class InitLoader {
	
	@Bean
	public ApplicationListener<ApplicationReadyEvent> initialLoader(ProjectVersionService projectVersionService, MicroserviceVisualizationProperties properties) {
		return event -> properties.getProjects()
				.forEach(projectVersionService::loadFolderProjects);
	}
}
