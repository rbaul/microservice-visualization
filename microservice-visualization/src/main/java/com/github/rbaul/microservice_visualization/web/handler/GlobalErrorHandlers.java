package com.github.rbaul.microservice_visualization.web.handler;

import com.github.rbaul.microservice_visualization.web.dto.errors.ErrorCodes;
import com.github.rbaul.microservice_visualization.web.dto.errors.ErrorDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * It is recommended to replace the messages with those
 * that do not reveal details about the code.
 */
@Slf4j
@RestControllerAdvice
public class GlobalErrorHandlers {
	
	@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(Exception.class)
	public ErrorDto handleGlobalError(Exception ex) {
		log.error("Global error handler exception: ", ex);
		return ErrorDto.builder()
				.errorCode(ErrorCodes.UNKNOWN.name())
				.message(ex.getLocalizedMessage())
				.build();
	}
	
	@ResponseStatus(code = HttpStatus.NOT_FOUND)
	@ExceptionHandler(EmptyResultDataAccessException.class)
	public ErrorDto handleEmptyResultDataAccessException(EmptyResultDataAccessException ex) {
		return ErrorDto.builder()
				.errorCode(ErrorCodes.NOT_FOUND.name())
				.message(ex.getLocalizedMessage())
				.build();
	}
}