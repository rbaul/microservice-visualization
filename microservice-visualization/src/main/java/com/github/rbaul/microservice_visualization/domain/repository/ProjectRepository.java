package com.github.rbaul.microservice_visualization.domain.repository;

import com.github.rbaul.microservice_visualization.domain.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Integer> {
}
