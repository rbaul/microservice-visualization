package com.github.rbaul.microservice_visualization.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;

@Slf4j
@UtilityClass
public class BitbucketConverterUtils {

    public static String getBranchIdFromName(String branchName) {
        return MessageFormat.format("refs/heads/{0}", branchName);
    }

    public static String getTagIdFromName(String tagName) {
        return MessageFormat.format("refs/tag/{0}", tagName);
    }
    public static String getFullName(String projectName, String repoName) {
        return MessageFormat.format("{0}/{1}", projectName, repoName);
    }
}
