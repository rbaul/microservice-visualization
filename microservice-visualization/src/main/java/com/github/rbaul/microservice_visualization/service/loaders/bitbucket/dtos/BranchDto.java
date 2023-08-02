package com.github.rbaul.microservice_visualization.service.loaders.bitbucket.dtos;

public record BranchDto(String id, String displayId, String type, String latestCommit, String latestChangeset,
                        Boolean isDefault) {
}
