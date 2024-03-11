package com.github.rbaul.microservice_visualization;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rbaul.microservice_visualization.service.model.ApplicationDependency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Import(ObjectMapper.class)
@ExtendWith({SpringExtension.class})
public class DependenciesConvertTests {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    @DisplayName("Validate dependencies files")
    void validateDependenciesFile() throws IOException {
        Resource resource = new ClassPathResource("basket.json");
        objectMapper.readValue(resource.getContentAsString(StandardCharsets.UTF_8), ApplicationDependency.class);
    }
}
