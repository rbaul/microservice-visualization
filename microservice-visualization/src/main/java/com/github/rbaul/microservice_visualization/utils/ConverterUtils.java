package com.github.rbaul.microservice_visualization.utils;

import com.github.rbaul.microservice_visualization.domain.model.DependencyManagement;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@UtilityClass
public class ConverterUtils {

    /**
     * Convert from "org.springframework.boot:spring-boot-starter-amqp:2.7.10" to Object
     */
    public static Dependency convertDependency(String dependency) {
        String[] split = dependency.split(":");
        if (split.length == 3) {
            return new Dependency(split[0], split[1], split[2]);
        } else { // Local Dependency
            return new Dependency(null, split[1], null);
        }
    }

    /**
     * Convert from "org.springframework.boot:spring-boot-starter-amqp" -> "2.7.10" to Object
     */
    public static Dependency convertDependency(String dependency, String version) {
        String[] split = dependency.split(":");
        if (split.length == 2) {
            return new Dependency(split[0], split[1], version);
        } else { // Local Dependency
            return new Dependency(null, split[1], version);
        }
    }

    /**
     * Convert to Dependency Management
     */
    public static DependencyManagement mapToDependencyManagement(String dependencyManagement, Map<String, String> dependencyManagementVersions) {
        Dependency dependency = convertDependency(dependencyManagement);
        return DependencyManagement.builder()
                .groupId(dependency.packageId())
                .artifactId(dependency.name())
                .version(dependency.version())
                .dependencies(dependencyManagementVersions.entrySet().stream()
                        .map(entry -> String.format("%s:%s", entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList())).build();
    }


    public static String getDependencyFormatted(String ...array) {
        return String.join(":", array);
    }
}
