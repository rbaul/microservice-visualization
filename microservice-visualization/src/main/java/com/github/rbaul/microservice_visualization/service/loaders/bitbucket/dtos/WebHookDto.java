package com.github.rbaul.microservice_visualization.service.loaders.bitbucket.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DiffDto(String type, String name) {
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

    public static void main(String[] args) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        WebHookDto webHookDto = objectMapper.readValue("{\"actor\":{\"username\":\"rbaul\",\"displayName\":\"Roman Baul\"},\"repository\":{\"scmId\":\"git\",\"project\":{\"key\":\"~RBAUL\",\"name\":\"Roman Baul\"},\"slug\":\"nc-muse-architecture\",\"links\":{\"self\":[{\"href\":\"https://ilptltvbbp01.ecitele.com:8443/users/rbaul/repos/nc-muse-architecture/browse\"}]},\"ownerName\":\"~RBAUL\",\"public\":true,\"fullName\":\"~RBAUL/nc-muse-architecture\",\"owner\":{\"username\":\"~RBAUL\",\"displayName\":\"~RBAUL\"}},\"push\":{\"changes\":[{\"created\":false,\"closed\":false,\"old\":{\"type\":\"branch\",\"name\":\"master\",\"target\":{\"type\":\"commit\",\"hash\":\"f44d713d00bc0d0697f8e0e55662119a3b296496\"}},\"new\":{\"type\":\"branch\",\"name\":\"master\",\"target\":{\"type\":\"commit\",\"hash\":\"f88a317393541a3c4bba2f94ddc41d6a4c8ae841\"}}}]}}",
                WebHookDto.class);
        System.out.println(webHookDto);

        webHookDto = objectMapper.readValue("{\"actor\":{\"username\":\"rbaul\",\"displayName\":\"Roman Baul\"},\"repository\":{\"scmId\":\"git\",\"project\":{\"key\":\"~RBAUL\",\"name\":\"Roman Baul\"},\"slug\":\"nc-muse-architecture\",\"links\":{\"self\":[{\"href\":\"https://ilptltvbbp01.ecitele.com:8443/users/rbaul/repos/nc-muse-architecture/browse\"}]},\"ownerName\":\"~RBAUL\",\"public\":true,\"fullName\":\"~RBAUL/nc-muse-architecture\",\"owner\":{\"username\":\"~RBAUL\",\"displayName\":\"~RBAUL\"}},\"push\":{\"changes\":[{\"created\":false,\"closed\":true,\"old\":{\"type\":\"branch\",\"name\":\"feature/nnnn\",\"target\":{\"type\":\"commit\",\"hash\":\"30c56ed4d452e36d7f9a55e938ac86fada0d9e1c\"}},\"new\":null}]}}",
                WebHookDto.class);
        System.out.println(webHookDto);

        webHookDto = objectMapper.readValue("{\"actor\":{\"username\":\"rbaul\",\"displayName\":\"Roman Baul\"},\"repository\":{\"scmId\":\"git\",\"project\":{\"key\":\"~RBAUL\",\"name\":\"Roman Baul\"},\"slug\":\"nc-muse-architecture\",\"links\":{\"self\":[{\"href\":\"https://ilptltvbbp01.ecitele.com:8443/users/rbaul/repos/nc-muse-architecture/browse\"}]},\"ownerName\":\"~RBAUL\",\"public\":true,\"fullName\":\"~RBAUL/nc-muse-architecture\",\"owner\":{\"username\":\"~RBAUL\",\"displayName\":\"~RBAUL\"}},\"push\":{\"changes\":[{\"created\":true,\"closed\":false,\"old\":null,\"new\":{\"type\":\"branch\",\"name\":\"feature/nnnn\",\"target\":{\"type\":\"commit\",\"hash\":\"30c56ed4d452e36d7f9a55e938ac86fada0d9e1c\"}}}]}}",
                WebHookDto.class);
        System.out.println(webHookDto);
    }
}


