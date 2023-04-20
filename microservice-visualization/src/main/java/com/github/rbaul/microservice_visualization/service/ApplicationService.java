package com.github.rbaul.microservice_visualization.service;

import com.github.rbaul.microservice_visualization.domain.model.Application;
import com.github.rbaul.microservice_visualization.domain.repository.ApplicationRepository;
import com.github.rbaul.microservice_visualization.web.dto.ApplicationFullDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ApplicationService {
	private final ApplicationRepository applicationRepository;
	
	private final ModelMapper modelMapper;
	
	@Transactional(readOnly = true)
	public ApplicationFullDto get(long id) {
		return getDto(getById(id));
	}
	
	private ApplicationFullDto getDto(Application application) {
		return modelMapper.map(application, ApplicationFullDto.class);
	}
	
	private Application getById(long id) {
		return applicationRepository.findById(id).orElseThrow(() -> new EmptyResultDataAccessException("No found application with id: " + id, 1));
	}
	
}
