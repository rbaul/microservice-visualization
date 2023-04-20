package com.github.rbaul.microservice_visualization.service.loaders;

import java.util.List;
import java.util.Map;

public record ApplicationDependency(
		String name,
		Map<String, String> tags,
		List<String> dependencies,
		List<String> managementDependencies
) {
}
