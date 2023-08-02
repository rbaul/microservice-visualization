package com.github.rbaul.microservice_visualization.domain.repository;

import com.github.rbaul.microservice_visualization.domain.model.ProjectVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectVersionRepository extends JpaRepository<ProjectVersion, Integer> {
    Optional<ProjectVersion> findByFullName(String fullName);
}
