package com.github.rbaul.microservice_visualization.web.dto.errors;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;

import java.time.Instant;
import java.util.Date;
import java.util.Set;

/**
 * It is recommended to replace the messages with those
 * that do not reveal details about the code.
 */
@Getter
@Setter
@Builder
public class ErrorDto {
	private final String errorCode;
	
	private final String message;
	
	@Builder.Default
	private final Date timestamp = Date.from(Instant.now());
	
	@Singular
	private final Set<Object> errors;
}