package com.github.rbaul.microservice_visualization.domain.repository;

import com.github.rbaul.microservice_visualization.domain.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
}
