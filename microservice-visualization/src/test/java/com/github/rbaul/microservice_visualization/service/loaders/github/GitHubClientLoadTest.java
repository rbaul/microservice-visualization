package com.github.rbaul.microservice_visualization.service.loaders.github;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class GitHubClientLoadTest {

    @Test
    @DisplayName("GitHub API client static initializer loads without Jackson error")
    void githubApiClientLoadsWithoutJacksonError() {
        assertDoesNotThrow(() -> Class.forName("org.kohsuke.github.GitHubClient"));
    }
}
