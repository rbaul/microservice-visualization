package com.github.rbaul.microservice_visualization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class MicroserviceVisualizationApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroserviceVisualizationApplication.class, args);
	}

}
