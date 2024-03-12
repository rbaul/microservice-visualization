package com.github.rbaul.microservice_visualization.domain.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class DependencyManagement {
    @NotEmpty
    private String groupId;
    @NotEmpty
    private String artifactId;
    @NotEmpty
    private String version;
    private List<@NotEmpty String> dependencies;
}
