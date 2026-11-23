package com.github.rbaul.microservice_visualization.service.loaders.bitbucket;

import com.github.rbaul.microservice_visualization.service.ProjectService;
import com.github.rbaul.microservice_visualization.service.loaders.ProjectNotificationType;
import com.github.rbaul.microservice_visualization.service.loaders.bitbucket.dtos.NativeWebHookDto;
import com.github.rbaul.microservice_visualization.service.loaders.bitbucket.dtos.WebHookDto;
import com.github.rbaul.microservice_visualization.utils.BitbucketConverterUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/bitbucket")
public class NotifierController {

    private final ProjectService projectService;

    @PostMapping("/notifier")
    public void get(@RequestBody WebHookDto webHookDto) {
        log.info("Received bitbucket notification: '{}'", webHookDto);

        if (webHookDto == null || webHookDto.repository() == null || webHookDto.repository().project() == null
                || webHookDto.push() == null || webHookDto.push().changes() == null || webHookDto.push().changes().isEmpty()) {
            log.warn("Received invalid or empty bitbucket notification");
            return;
        }

        String repositoryName = webHookDto.repository().slug();
        String projectName = webHookDto.repository().project().key();
        if (projectName == null || repositoryName == null) {
            log.warn("Bitbucket notification missing project or repository name");
            return;
        }

        projectName = projectName.replace("~", ""); // Fixed user private project
        String fullName = BitbucketConverterUtils.getFullName(projectName, repositoryName);

        for (WebHookDto.ChangeDto change : webHookDto.push().changes()) {
            if (change == null) {
                continue;
            }

            if (Boolean.TRUE.equals(change.created()) && change.neww() != null
                    && change.neww().type() == WebHookDto.DiffType.BRANCH) {
                log.info("Branch created: '{}' in '{}'", change.neww().name(), fullName);
                projectService.create(fullName, change.neww().name());
            } else if (Boolean.TRUE.equals(change.closed()) && change.old() != null
                    && change.old().type() == WebHookDto.DiffType.BRANCH) {
                log.info("Branch deleted: '{}' in '{}'", change.old().name(), fullName);
                projectService.delete(fullName, change.old().name());
            } else if (change.old() != null && change.old().type() == WebHookDto.DiffType.BRANCH) {
                log.info("Branch modified: '{}' in '{}'", change.old().name(), fullName);
                projectService.update(fullName, change.old().name());
            }
        }
    }

    /**
     * Bitbucket Data Center native webhook endpoint (repo:refs_changed event).
     */
    @PostMapping("/notifier/native")
    public void nativeNotifier(@RequestBody NativeWebHookDto nativeWebHookDto) {
        log.info("Received bitbucket native notification: '{}'", nativeWebHookDto);

        if (nativeWebHookDto == null || nativeWebHookDto.repository() == null
                || nativeWebHookDto.repository().project() == null) {
            log.warn("Received invalid or empty bitbucket native notification");
            return;
        }

        String repositoryName = nativeWebHookDto.repository().slug();
        String projectName = nativeWebHookDto.repository().project().key();
        if (projectName == null || repositoryName == null) {
            log.warn("Bitbucket native notification missing project or repository name");
            return;
        }

        projectName = projectName.replace("~", ""); // Fixed user private project
        String fullName = BitbucketConverterUtils.getFullName(projectName, repositoryName);

        List<NativeWebHookDto.ChangeDto> changes = nativeWebHookDto.changes();
        if (changes == null) {
            changes = Collections.emptyList();
        }

        for (NativeWebHookDto.ChangeDto change : changes) {
            if (change == null || change.ref() == null || change.type() == null) {
                continue;
            }

            if (change.ref().type() != NativeWebHookDto.RefType.BRANCH) {
                continue;
            }

            String branchName = change.ref().displayId();
            if (branchName == null || branchName.isBlank()) {
                continue;
            }

            switch (change.type()) {
                case ADD -> {
                    log.info("Branch created: '{}' in '{}'", branchName, fullName);
                    projectService.create(fullName, branchName);
                }
                case DELETE -> {
                    log.info("Branch deleted: '{}' in '{}'", branchName, fullName);
                    projectService.delete(fullName, branchName);
                }
                case UPDATE -> {
                    log.info("Branch updated: '{}' in '{}'", branchName, fullName);
                    projectService.update(fullName, branchName);
                }
            }
        }
    }
}
