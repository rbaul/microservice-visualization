package com.github.rbaul.microservice_visualization.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class ProjectDto {
	private Short id;
	private String version;
	private List<ApplicationLiteDto> applications;
	
	private Map<String, List<String>> connections;
	
	private List<ApplicationGroupDto> groups;
	
	private List<OwnerDto> owners;
	
	private ProjectVersionLiteDto projectVersion;
}
