package com.github.rbaul.microservice_visualization.exception;

public class MicroserviceVisualizationException extends RuntimeException {
    public MicroserviceVisualizationException(String message) {
        super(message);
    }

    public MicroserviceVisualizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
