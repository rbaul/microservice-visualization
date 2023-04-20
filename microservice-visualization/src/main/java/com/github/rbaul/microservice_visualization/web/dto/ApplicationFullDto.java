package com.github.rbaul.microservice_visualization.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@ToString
public class ApplicationFullDto extends ApplicationLiteDto {
	
	private List<String> dependencies;
	
	private List<String> managementDependencies;
}
