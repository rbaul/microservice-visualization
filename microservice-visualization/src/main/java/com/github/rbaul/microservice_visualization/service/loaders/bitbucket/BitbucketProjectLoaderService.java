package com.github.rbaul.microservice_visualization.service.loaders.bitbucket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rbaul.microservice_visualization.config.MicroserviceVisualizationProperties;
import com.github.rbaul.microservice_visualization.domain.model.Application;
import com.github.rbaul.microservice_visualization.domain.model.ApplicationType;
import com.github.rbaul.microservice_visualization.domain.model.LoaderDetails;
import com.github.rbaul.microservice_visualization.domain.model.Project;
import com.github.rbaul.microservice_visualization.service.loaders.ProjectLoaderService;
import com.github.rbaul.microservice_visualization.service.loaders.ProjectLoaderType;
import com.github.rbaul.microservice_visualization.service.loaders.bitbucket.dtos.BranchDto;
import com.github.rbaul.microservice_visualization.service.model.ProjectConfig;
import com.github.rbaul.microservice_visualization.utils.BitbucketConverterUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BitbucketProjectLoaderService extends ProjectLoaderService {
    public BitbucketProjectLoaderService(MicroserviceVisualizationProperties properties, ObjectMapper objectMapper) {
        super(properties, objectMapper);
    }

    @Override
    public List<Project> retrieveProject(MicroserviceVisualizationProperties.ProjectDetails projectProperty) {

        BitBucketV1Api api = BitBucketV1Api.getBitBucketV1Api(projectProperty.getUrl(), projectProperty.getToken());

        List<BranchDto> allBranches = api.getAllBranches(projectProperty.getProject(), projectProperty.getRepo()); // TODO: default branch it's default version
        List<Project> projects = allBranches.stream().map(branchDto -> getProjectByVersion(LoaderDetails.builder().repo(projectProperty.getRepo())
                .project(projectProperty.getProject()).build(), branchDto.id(), branchDto.displayId(), api)).collect(Collectors.toList());

        return projects;
//        List<BranchDto> allTags = getAllTags(projectProperty.getProject(), projectProperty.getRepo());
    }

    /**
     * Get project by version
     */
    @Override
    public Project getProjectVersion(LoaderDetails loaderDetails, String versionId, String version) {
        BitBucketV1Api api = BitBucketV1Api.getBitBucketV1Api(loaderDetails.getUrl(), loaderDetails.getToken());

        return getProjectByVersion(loaderDetails, versionId, version, api);
    }

    private Project getProjectByVersion(LoaderDetails loaderDetails, String versionId, String version, BitBucketV1Api api) {
        Project project = new Project();
        ProjectConfig projectConfigByBranch = getProjectConfig(api.getProjectConfigByBranch(loaderDetails.getProject(), loaderDetails.getRepo(), versionId));

        List<Application> applicationDependencies = getApplicationsByFolder(loaderDetails, versionId, api, ProjectLoaderService.APPLICATIONS_FOLDER, ApplicationType.MICROSERVICE);

        List<Application> libraryDependencies = getApplicationsByFolder(loaderDetails, versionId, api, ProjectLoaderService.LIBRARIES_FOLDER, ApplicationType.LIBRARY);

        List<Application> bomDependencies = getApplicationsByFolder(loaderDetails, versionId, api, ProjectLoaderService.BOMS_FOLDER, ApplicationType.BOM);

        List<Application> dependencies = new ArrayList<>(applicationDependencies);
        dependencies.addAll(libraryDependencies);
        dependencies.addAll(bomDependencies);

        setApplicationToProject(project, projectConfigByBranch, dependencies, versionId, version);
        return project;
    }

    private List<Application> getApplicationsByFolder(LoaderDetails loaderDetails, String versionId, BitBucketV1Api api, String folderName, ApplicationType defaultType) {
        try {
            List<String> allApplicationsByBranch = api.getAllApplicationsByBranch(loaderDetails.getProject(), loaderDetails.getRepo(), versionId, folderName);

            return allApplicationsByBranch.stream()
                    .map(applicationFileName -> readContent(loaderDetails, versionId, api, folderName, applicationFileName)
                            .flatMap(contentString -> convertContentToApplication(contentString, defaultType)))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed load folder: '{}'", folderName, e);
            return List.of();
        }
    }

    protected Optional<String> readContent(LoaderDetails loaderDetails, String versionId, BitBucketV1Api api, String folderName, String applicationFileName) {
        try {
            return Optional.of(api.getApplicationByBranch(loaderDetails.getProject(), loaderDetails.getRepo(), versionId, folderName, applicationFileName));
        } catch (Exception e) {
            log.error("Failed read application file {}/{}", folderName, applicationFileName, e);
            return Optional.empty();
        }
    }

    @Override
    protected String getFullName(MicroserviceVisualizationProperties.ProjectDetails projectProperty) {
        return BitbucketConverterUtils.getFullName(projectProperty.getProject(), projectProperty.getRepo());
    }

    @Override
    public ProjectLoaderType getType() {
        return ProjectLoaderType.bitbucket;
    }
}
