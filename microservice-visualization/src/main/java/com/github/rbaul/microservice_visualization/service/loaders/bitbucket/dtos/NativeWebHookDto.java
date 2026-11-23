package com.github.rbaul.microservice_visualization.service.loaders.bitbucket.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Bitbucket Data Center native webhook payload (repo:refs_changed event).
 *
 * @see <a href="https://confluence.atlassian.com/bitbucketserver/event-payload-938025882.html">Event Payload</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NativeWebHookDto(String eventKey, ActorDto actor, RepositoryDto repository, List<ChangeDto> changes) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ActorDto(String name, String slug, String displayName) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RepositoryDto(String scmId, String slug,
                                ProjectDto project,
                                @JsonProperty("public") Boolean isPublic) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProjectDto(String key, String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChangeDto(RefDto ref, ChangeType type) {
    }

    /**
     * @param id        - full ref (e.g., refs/heads/main)
     * @param displayId - short name (e.g., main)
     * @param type      - BRANCH or TAG
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RefDto(String id, String displayId, RefType type) {
    }

    @RequiredArgsConstructor
    public enum ChangeType {
        ADD("ADD"),
        UPDATE("UPDATE"),
        DELETE("DELETE");

        @Getter(onMethod_ = @JsonValue)
        private final String value;
    }

    @RequiredArgsConstructor
    public enum RefType {
        BRANCH("BRANCH"),
        TAG("TAG");

        @Getter(onMethod_ = @JsonValue)
        private final String value;
    }

}
