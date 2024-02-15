package com.github.rbaul.microservice_visualization.web.dto;

import lombok.*;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class DependencyDto {
    @EqualsAndHashCode.Include
    private String packageName;

    @EqualsAndHashCode.Include
    private String artifactName;

    @EqualsAndHashCode.Include
    private String version;

    @Builder.Default
    private Set<String> usageOf = new HashSet<>();

}
