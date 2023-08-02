package com.github.rbaul.microservice_visualization.domain.repository;

import com.github.rbaul.microservice_visualization.domain.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Integer> {
    Page<Project> findByProjectVersionId(int id, Pageable pageable);

    Optional<Project> findByProjectVersionFullNameAndVersion(String name, String version);
}
