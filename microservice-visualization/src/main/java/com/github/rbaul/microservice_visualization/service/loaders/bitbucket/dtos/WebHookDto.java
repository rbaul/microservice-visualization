package com.github.rbaul.microservice_visualization.service.loaders.bitbucket.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WebHookDto(ActorDto actor, RepositoryDto repository, PushDto push) {

    public record ActorDto(String username, String displayName) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RepositoryDto(String scmId, String slug,
                                ProjectDto project,
                                @JsonProperty("public") Boolean isPublic,
                                String fullName) {
    }

    /**
     * @param type - branch/tag
     * @param name
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DiffDto(DiffType type, String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProjectDto(String key, String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChangeDto(Boolean created, Boolean closed,
                            DiffDto old, @JsonProperty("new") DiffDto neww) {
    }

    public record PushDto(List<ChangeDto> changes) {
    }

    @RequiredArgsConstructor
    public enum DiffType {
        BRANCH("branch"),
        TAG("tag");

        @Getter(onMethod_ = @JsonValue)
        private final String value;
    }

}


