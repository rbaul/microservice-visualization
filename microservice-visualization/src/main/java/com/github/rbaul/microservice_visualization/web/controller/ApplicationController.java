package com.github.rbaul.microservice_visualization.web.controller;

import com.github.rbaul.microservice_visualization.service.ApplicationService;
import com.github.rbaul.microservice_visualization.web.dto.ApplicationFullDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/applications")
public class ApplicationController {
	
	private final ApplicationService applicationService;
	
	@GetMapping("{id}")
	public ApplicationFullDto get(@PathVariable("id") int id) {
		return applicationService.get(id);
	}
	
}
