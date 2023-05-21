package com.github.rbaul.microservice_visualization.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

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
	
}
