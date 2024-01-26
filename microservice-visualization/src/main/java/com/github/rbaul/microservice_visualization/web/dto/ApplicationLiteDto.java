package com.github.rbaul.microservice_visualization.web.dto;

import com.github.rbaul.microservice_visualization.domain.model.ApplicationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Map;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@ToString
public class ApplicationLiteDto {
	
	private Long id;
	
	private String name;
	
	private String label;
	
	private String description;

	private ApplicationType type;

	private String group;

	private String version;

	private String location;
	
	private Set<String> owners;
	
	private Map<String, String> tags;
}
