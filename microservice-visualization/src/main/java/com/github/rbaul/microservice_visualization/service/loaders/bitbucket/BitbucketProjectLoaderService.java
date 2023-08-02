package com.github.rbaul.microservice_visualization.service.loaders.bitbucket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rbaul.microservice_visualization.config.MicroserviceVisualizationProperties;
import com.github.rbaul.microservice_visualization.domain.model.Application;
import com.github.rbaul.microservice_visualization.domain.model.LoaderDetails;
import com.github.rbaul.microservice_visualization.domain.model.Project;
import com.github.rbaul.microservice_visualization.service.loaders.ProjectLoaderService;
import com.github.rbaul.microservice_visualization.service.loaders.ProjectLoaderType;
import com.github.rbaul.microservice_visualization.service.loaders.bitbucket.dtos.BranchDto;
import com.github.rbaul.microservice_visualization.service.model.ProjectConfig;
import com.github.rbaul.microservice_visualization.utils.BitbucketConverterUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
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
        ProjectConfig projectConfigByBranch = getProjectConfig(api.getProjectConfigByBranch(loaderDetails.getProject(), loaderDetails.getRepo(), versionId));
        List<String> allApplicationsByBranch = api.getAllApplicationsByBranch(loaderDetails.getProject(), loaderDetails.getRepo(), versionId);
        Project project = new Project();
        List<Application> applicationDependencies = allApplicationsByBranch.stream().map(applicationFileName ->
                        convertApplicationDependencyToApplication(getApplicationDependency(api.getApplicationByBranch(loaderDetails.getProject(), loaderDetails.getRepo(), versionId, applicationFileName))))
                .collect(Collectors.toList());


        setApplicationToProject(project, projectConfigByBranch, applicationDependencies, versionId, version);
        return project;
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
