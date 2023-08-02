package com.github.rbaul.microservice_visualization.web.controller;

import com.github.rbaul.microservice_visualization.service.ProjectService;
import com.github.rbaul.microservice_visualization.web.dto.ProjectDto;
import com.github.rbaul.microservice_visualization.web.dto.ProjectLiteDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("search")
    public Page<ProjectLiteDto> fetch(@RequestParam int projectVersionId,
                                      @PageableDefault Pageable pageable) {
        return projectService.search(projectVersionId, pageable);
    }

}
