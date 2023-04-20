package com.github.rbaul.microservice_visualization.service;

import com.github.rbaul.microservice_visualization.config.MicroserviceVisualizationProperties;
import com.github.rbaul.microservice_visualization.domain.model.ProjectVersion;
import com.github.rbaul.microservice_visualization.domain.repository.ProjectVersionRepository;
import com.github.rbaul.microservice_visualization.service.loaders.ProjectLoaderFactory;
import com.github.rbaul.microservice_visualization.service.loaders.ProjectLoaderType;
import com.github.rbaul.microservice_visualization.web.dto.ProjectVersionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProjectVersionService {
	private final ProjectVersionRepository projectVersionRepository;
	
	private final ModelMapper modelMapper;
	
	private final ProjectLoaderFactory projectLoaderFactory;
	
	public void loadFolderProjects(MicroserviceVisualizationProperties.MicroserviceVisualizationProject projectProperty) {
		projectLoaderFactory.getByType(ProjectLoaderType.FOLDER)
				.ifPresentOrElse(projectLoaderService -> projectLoaderService.retrieve(projectProperty)
						.ifPresent(projectVersionRepository::save), () -> log.error("Not found project loader of {}", ProjectLoaderType.FOLDER));
	}
	
	@Transactional(readOnly = true)
	public ProjectVersionDto get(int id) {
		return getDto(getById(id));
	}
	
	private ProjectVersion getById(int id) {
		return projectVersionRepository.findById(id)
				.orElseThrow(() -> new EmptyResultDataAccessException("No found project version with id: " + id, 1));
	}
	
	@Transactional(readOnly = true)
	public Page<ProjectVersionDto> search(String filter, Pageable pageable) {
		return projectVersionRepository.findAll(pageable).map(this::getDto);
	}
	
	private ProjectVersionDto getDto(ProjectVersion projectVersion) {
		return modelMapper.map(projectVersion, ProjectVersionDto.class);
	}
	
	public void delete(int id) {
		projectVersionRepository.deleteById(id);
	}
}
