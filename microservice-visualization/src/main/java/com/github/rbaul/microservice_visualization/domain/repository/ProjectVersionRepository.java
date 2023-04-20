package com.github.rbaul.microservice_visualization.domain.repository;

import com.github.rbaul.microservice_visualization.domain.model.ProjectVersion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectVersionRepository extends JpaRepository<ProjectVersion, Integer> {
}
