package com.github.rbaul.microservice_visualization.service.loaders.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rbaul.microservice_visualization.config.MicroserviceVisualizationProperties;
import com.github.rbaul.microservice_visualization.domain.model.Application;
import com.github.rbaul.microservice_visualization.domain.model.LoaderDetails;
import com.github.rbaul.microservice_visualization.domain.model.Project;
import com.github.rbaul.microservice_visualization.service.loaders.ProjectLoaderService;
import com.github.rbaul.microservice_visualization.service.loaders.ProjectLoaderType;
import com.github.rbaul.microservice_visualization.service.model.ProjectConfig;
import com.github.rbaul.microservice_visualization.utils.BitbucketConverterUtils;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GithubProjectLoaderService extends ProjectLoaderService {
    public GithubProjectLoaderService(MicroserviceVisualizationProperties properties, ObjectMapper objectMapper) {
        super(properties, objectMapper);
    }

    @Override
    public List<Project> retrieveProject(MicroserviceVisualizationProperties.ProjectDetails projectProperty) {
        try {
            GitHub gitHub = GitHub.connectAnonymously();
            GHRepository projectRepository = gitHub.getRepository(getFullName(projectProperty));
            Map<String, GHBranch> branches = projectRepository.getBranches();
            List<Project> projects = branches.values().stream()
                    .map(ghBranch -> getProjectByVersion(ghBranch.getName(), ghBranch.getName(), projectRepository))
                    .collect(Collectors.toList());

            return projects;
        } catch (IOException e) {
            return List.of();
        }
    }

    /**
     * Get project by version
     */
    @Override
    public Project getProjectVersion(LoaderDetails loaderDetails, String versionId, String version) {
        try {
            GitHub gitHub = GitHub.connect();
            GHRepository projectRepository = gitHub.getRepository(BitbucketConverterUtils.getFullName(loaderDetails.getProject(), loaderDetails.getRepo()));
            return getProjectByVersion(versionId, version, projectRepository);
        } catch (IOException e) {
            log.error("Failed get project version", e);
            return null;
        }
    }

    private Project getProjectByVersion(String versionId, String version, GHRepository projectRepository) {
        Project project = new Project();
        try {
            GHContent fileContent = projectRepository.getFileContent(ProjectLoaderService.PROJECT_CONFIG_YAML, versionId);

            ProjectConfig projectConfigByBranch = getProjectConfig(fileContent.getContent());

            List<GHContent> directoryContent = projectRepository.getDirectoryContent(ProjectLoaderService.APPLICATIONS_FOLDER, versionId);

            List<Application> applicationDependencies = directoryContent.stream().map(ghContent -> {
                        try {
                            String encodedContent = projectRepository.getFileContent(ghContent.getPath(), versionId).getContent();
                            return convertApplicationDependencyToApplication(getApplicationDependency(encodedContent));
                        } catch (IOException e) {
                            log.error("Failed retrieve content of '{}'", ghContent.getPath(), e);
                            return null;
                        }
                    }).filter(Objects::nonNull)
                    .collect(Collectors.toList());
            setApplicationToProject(project, projectConfigByBranch, applicationDependencies, versionId, version);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return project;
    }

    @Override
    protected String getFullName(MicroserviceVisualizationProperties.ProjectDetails projectProperty) {
        return BitbucketConverterUtils.getFullName(projectProperty.getProject(), projectProperty.getRepo());
    }

    @Override
    public ProjectLoaderType getType() {
        return ProjectLoaderType.github;
    }
}
