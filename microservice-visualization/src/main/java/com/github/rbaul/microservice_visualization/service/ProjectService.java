package com.github.rbaul.microservice_visualization.service;

import com.github.rbaul.microservice_visualization.domain.model.Project;
import com.github.rbaul.microservice_visualization.domain.repository.ProjectRepository;
import com.github.rbaul.microservice_visualization.web.dto.ProjectDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProjectService {
	private final ProjectRepository projectRepository;
	
	private final ModelMapper modelMapper;
	
	@Transactional(readOnly = true)
	public ProjectDto get(int id) {
		return getDto(getById(id));
	}
	
	private Project getById(int id) {
		return projectRepository.findById(id)
				.orElseThrow(() -> new EmptyResultDataAccessException("No found project with id: " + id, 1));
	}
	
	public ProjectDto create(ProjectDto projectDto) {
		return null; // TODO: Support crate project on the fly
	}
	
	public void delete(int id) {
		projectRepository.deleteById(id);
	}
	
	private ProjectDto getDto(Project project) {
		return modelMapper.map(project, ProjectDto.class);
	}
	
	@Transactional
	public void mergeProjects(int projectAId, int projectBId) {
		Project projectA = getById(projectAId);
		Project projectB = getById(projectBId);
		// TODO: Merge
	}
	
}
