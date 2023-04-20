package com.github.rbaul.microservice_visualization.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class ProjectVersionDto {
	private Short id;
	private String name;
	private String description;
	private ProjectLiteDto mainProject;
	private List<ProjectLiteDto> projects;
}
