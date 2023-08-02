package com.github.rbaul.microservice_visualization.domain.model;

import com.github.rbaul.microservice_visualization.service.loaders.ProjectLoaderType;
import jakarta.persistence.Embeddable;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
@Embeddable
public class LoaderDetails {
    private String url;
    private String project;
    private String repo;
    private String token;
    private String folder;
    private ProjectLoaderType loaderType;
}
