package com.github.rbaul.microservice_visualization.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum ApplicationType {
    MICROSERVICE("microservice"),
    LIBRARY("library"),
    BOM("bom");

    private final String value;

    public static Optional<ApplicationType> getType(String type) {
        return Arrays.stream(ApplicationType.values())
                .filter(applicationType -> applicationType.getValue().equals(type)).findFirst();
    }
}
