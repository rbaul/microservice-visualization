package com.github.rbaul.microservice_visualization.service.loaders.bitbucket;

import com.github.rbaul.microservice_visualization.service.ProjectService;
import com.github.rbaul.microservice_visualization.service.loaders.ProjectNotificationType;
import com.github.rbaul.microservice_visualization.service.loaders.bitbucket.dtos.WebHookDto;
import com.github.rbaul.microservice_visualization.utils.BitbucketConverterUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/bitbucket")
public class NotifierController {

    private final ProjectService projectService;

    @PostMapping("/notifier")
    public void get(@RequestBody WebHookDto webHookDto) {
        log.info("Received bitbucket notification: '{}'", webHookDto);
        String repositoryName = webHookDto.repository().slug();
        WebHookDto.ChangeDto change = webHookDto.push().changes().get(0);
        String projectName = webHookDto.repository().project().key().toLowerCase().substring(1);
        ProjectNotificationType notificationType = null;
        String branchName = "";
        if (change.created()) { // Branch created
            notificationType = ProjectNotificationType.CREATED;
            branchName = change.neww().name();
            projectService.create(BitbucketConverterUtils.getFullName(projectName, repositoryName), branchName);
        } else if (change.closed()) { // Branch deleted
            notificationType = ProjectNotificationType.DELETED;
            branchName = change.old().name();
            projectService.delete(BitbucketConverterUtils.getFullName(projectName, repositoryName), branchName);
        } else { // Branch modified
            notificationType = ProjectNotificationType.MODIFIED;
            branchName = change.old().name();
            projectService.update(BitbucketConverterUtils.getFullName(projectName, repositoryName), branchName);
        }


    }

}
