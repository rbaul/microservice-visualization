package com.github.rbaul.microservice_visualization.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectConfig {
    private String applicationPostfix = "";

    private List<String> applicationApiPostfixes = List.of("-api");

    private List<GroupConfig> groups;

    private List<OwnerConfig> owners;

    @Getter
    @Setter
    public static class GroupConfig {
        private String name;
        private String description;
        private List<String> applicationNames;
    }

    @Getter
    @Setter
    public static class OwnerConfig {
        private String name;
        private String description;
        private List<String> applicationNames;
    }

}
