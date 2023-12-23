package com.github.rbaul.microservice_visualization.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Map;

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

	private String location;
	
	private String owner;
	
	private Map<String, String> tags;
}
