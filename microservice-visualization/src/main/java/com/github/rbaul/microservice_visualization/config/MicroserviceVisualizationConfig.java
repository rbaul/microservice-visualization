package com.github.rbaul.microservice_visualization.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MicroserviceVisualizationConfig {
	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
}
