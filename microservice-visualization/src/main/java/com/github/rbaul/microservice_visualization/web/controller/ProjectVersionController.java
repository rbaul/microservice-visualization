package com.github.rbaul.microservice_visualization.web.controller;

import com.github.rbaul.microservice_visualization.service.ProjectVersionService;
import com.github.rbaul.microservice_visualization.web.dto.ProjectVersionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/project-versions")
public class ProjectVersionController {
	
	private final ProjectVersionService projectVersionService;
	
	@GetMapping("{id}")
	public ProjectVersionDto get(@PathVariable int id) {
		return projectVersionService.get(id);
	}
	
	@PostMapping
	public ProjectVersionDto create(@RequestBody ProjectVersionDto projectDto) {
		return null;
	}
	
	@PutMapping("{id}")
	public ProjectVersionDto update(@PathVariable int id, @RequestBody ProjectVersionDto projectDto) {
		return null; //projectService.update(projectId, projectDto);
	}
	
	@DeleteMapping("{id}")
	public void delete(@PathVariable int id) {
		projectVersionService.delete(id);
	}
	
	@GetMapping("search")
	public Page<ProjectVersionDto> fetch(@RequestParam(required = false) String filter, @PageableDefault Pageable pageable) {
		return projectVersionService.search(filter, pageable);
	}
}
