package com.github.rbaul.microservice_visualization.service.loaders.bitbucket.dtos;

public record CommitDto(String id, String displayId, Long committerTimestamp, String message, UserDto author) {
}
