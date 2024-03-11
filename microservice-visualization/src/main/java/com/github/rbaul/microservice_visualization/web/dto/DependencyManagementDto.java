package com.github.rbaul.microservice_visualization.web.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@ToString
public class DependencyManagementDto {
    private String groupId;
    private String artifactId;
    private String version;
    private List<String> dependencies;
}
