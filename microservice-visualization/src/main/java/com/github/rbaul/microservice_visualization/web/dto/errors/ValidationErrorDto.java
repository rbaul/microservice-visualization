package com.github.rbaul.microservice_visualization.web.dto.errors;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;

import java.util.List;

@Getter
@Setter
@Builder
public class ValidationErrorDto {
	
	private String fieldName;
	
	private String errorCode;
	
	private Object rejectedValue;
	
	@Singular
	private List<Object> params;
	
	private String message;
}