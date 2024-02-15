package com.github.rbaul.microservice_visualization.web.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@ToString
public class ProjectDependenciesDto extends ProjectLiteDto {
	private Set<DependencyDto> dependencies;

	private ProjectVersionLiteDto projectVersion;
}
