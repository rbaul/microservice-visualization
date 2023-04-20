package com.github.rbaul.microservice_visualization.web.controller;

import com.github.rbaul.microservice_visualization.service.ProjectService;
import com.github.rbaul.microservice_visualization.web.dto.ProjectDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/projects")
public class ProjectController {
	
	private final ProjectService projectService;
	
	@GetMapping("{id}")
	public ProjectDto get(@PathVariable int id) {
		return projectService.get(id);
	}
	
	@PostMapping
	public ProjectDto create(@RequestBody ProjectDto projectDto) {
		return projectService.create(projectDto);
	}
	
	@PutMapping("{id}")
	public ProjectDto update(@PathVariable int id, @RequestBody ProjectDto projectDto) {
		return null; //projectService.update(projectId, projectDto);
	}
	
	@DeleteMapping("{id}")
	public void delete(@PathVariable int id) {
		projectService.delete(id);
	}
	
}
