package com.github.rbaul.microservice_visualization.domain.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class DependencyEntity {
    @NotEmpty
    private String groupId;
    @NotEmpty
    private String artifactId;
    @NotEmpty
    private String version;
    @NotNull
    private Boolean implicit;
}
