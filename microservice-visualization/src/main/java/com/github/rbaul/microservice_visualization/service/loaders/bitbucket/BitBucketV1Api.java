package com.github.rbaul.microservice_visualization.service.loaders.bitbucket;

import com.github.rbaul.microservice_visualization.service.loaders.ProjectLoaderService;
import com.github.rbaul.microservice_visualization.service.loaders.bitbucket.dtos.BranchDto;
import com.github.rbaul.microservice_visualization.service.loaders.bitbucket.dtos.CommitDto;
import com.github.rbaul.microservice_visualization.service.loaders.bitbucket.dtos.PagedDto;
import com.github.rbaul.microservice_visualization.service.loaders.bitbucket.dtos.UserDto;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

/**
 * Reference
 *
 * @see <a href="https://docs.atlassian.com/bitbucket-server/rest/4.13.0/bitbucket-rest.html">Bitbucket Rest API</a>
 */
//@HttpExchange(url = "/rest/api/1.0/users/{project}/repos/{repo}")
@HttpExchange(url = "/rest/api/1.0/projects/{project}/repos/{repo}")
public interface BitBucketV1Api {

    int MAX_LIMIT = 1000;

    String AUTHORIZATION = "Authorization";

    @GetExchange("/branches")
    PagedDto<BranchDto> getBranches(@PathVariable("project") String project, @PathVariable("repo") String repo,
                                    @RequestParam(name = "limit", required = false) Integer limit);

    @GetExchange("/branches/default")
    BranchDto getDefaultBranch(@PathVariable("project") String project, @PathVariable("repo") String repo);

    @GetExchange("/tags")
    PagedDto<BranchDto> getTags(@PathVariable("project") String project, @PathVariable("repo") String repo,
                                @RequestParam(name = "limit", required = false) Integer limit);

    @GetExchange("/files/{path}")
    PagedDto<String> getFiles(@PathVariable("project") String project, @PathVariable("repo") String repo,
                              @PathVariable(name = "path", required = false) String path,
                              @RequestParam(name = "limit", required = false) Integer limit,
                              @RequestParam(name = "at", required = false) String at);

    @GetExchange("/raw/{path}")
    String getFileContent(@PathVariable("project") String project, @PathVariable("repo") String repo,
                          @PathVariable("path") String path,
                          @RequestParam(name = "at", required = false) String at);

    @GetExchange("/commits")
    PagedDto<CommitDto> getCommits(@PathVariable("project") String project, @PathVariable("repo") String repo,
                                   @RequestParam(name = "limit", required = false) Integer limit,
                                   @RequestParam(name = "at", required = false) String at);

    @GetExchange("/commits/{commitId}")
    CommitDto getCommit(@PathVariable("project") String project, @PathVariable("repo") String repo,
                        @PathVariable(name = "commitId") String commitId);

    @GetExchange("/participants")
    PagedDto<UserDto> getParticipants(@PathVariable("project") String project, @PathVariable("repo") String repo,
                                      @RequestParam(name = "limit", required = false) Integer limit,
                                      @RequestParam(name = "direction", required = false) String direction,
                                      @RequestParam(name = "role", required = false) String role,
                                      @RequestParam(name = "at", required = false) String at);

    /**
     * Get all branches
     */
    default List<BranchDto> getAllBranches(String project, String repo) {
        return getBranches(project, repo, BitBucketV1Api.MAX_LIMIT).getValues();
    }

    /**
     * Get all tags
     */
    default List<BranchDto> getAllTags(String project, String repo) {
        return getTags(project, repo, MAX_LIMIT).getValues();
    }

    /**
     * Get all applications by branch/tag
     */
    default List<String> getAllApplicationsByBranch(String project, String repo, String branchId) {
        return getFiles(project, repo, ProjectLoaderService.APPLICATIONS_FOLDER, MAX_LIMIT, branchId).getValues();
    }

    /**
     * Get Application by Branch/Tag
     */
    default String getApplicationByBranch(String project, String repo, String branchId, String fileName) {
        return getFileContent(project, repo,
                MessageFormat.format("{0}/{1}", ProjectLoaderService.APPLICATIONS_FOLDER, fileName), branchId);
    }

    /**
     * Get Project Configuration by Branch/Tag
     */
    default String getProjectConfigByBranch(String project, String repo, String branchId) {
        return getFileContent(project, repo, ProjectLoaderService.PROJECT_CONFIG_YAML, branchId);
    }

    /**
     * Get Latest commit by branch/tag
     */
    default Optional<CommitDto> getLatestCommitByBranch(String project, String repo, String branchId) {
        return getCommits(project, repo, 1, branchId)
                .getValues().stream().findFirst();
    }

    static BitBucketV1Api getBitBucketV1Api(String baseUrl, String token) {

        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        WebClient.Builder webClientBuilder = WebClient.builder()
                .baseUrl(baseUrl)
                .uriBuilderFactory(factory)
                .defaultHeader(AUTHORIZATION, token);

        try {
            SslContext sslContext = SslContextBuilder
                    .forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();

            HttpClient httpClient = HttpClient.create().secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
            webClientBuilder.clientConnector(new ReactorClientHttpConnector(httpClient));
        } catch (SSLException e) {
            System.out.println("Failed disable SSL");
        }

        WebClient webClient = webClientBuilder.build();

        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builder(WebClientAdapter.forClient(webClient))
                .build();
        return httpServiceProxyFactory.createClient(BitBucketV1Api.class);
    }
}
