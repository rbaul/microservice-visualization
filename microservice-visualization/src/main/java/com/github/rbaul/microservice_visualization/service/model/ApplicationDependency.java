package com.github.rbaul.microservice_visualization.service.model;

import java.util.List;
import java.util.Map;

public record ApplicationDependency(
        String name,
        String label,
        String description,
        String owner,
        Map<String, String> tags,
        List<String> dependencies,
        List<String> managementDependencies
) {
}
