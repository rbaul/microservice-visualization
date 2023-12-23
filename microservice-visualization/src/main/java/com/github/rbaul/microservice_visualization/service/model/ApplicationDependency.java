package com.github.rbaul.microservice_visualization.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApplicationDependency(
        String name,
        String label,
        String description,
        String location,
        String owner,
        Map<String, String> tags,
        List<String> dependencies,
        List<String> managementDependencies
) {
}
