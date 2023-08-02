package com.github.rbaul.microservice_visualization.service.loaders.bitbucket.dtos;

public record UserDto(String id, String name, String emailAddress, String displayName, Boolean active) {
}
