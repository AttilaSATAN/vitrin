package com.tusas.vitrin.domain.exception;

/**
 * Thrown when a Kubernetes-specific operation is attempted while the application
 * is not running inside a Kubernetes cluster.
 */
public class NotInKubernetesException extends RuntimeException {

    /**
     * Constructs the exception with a fixed diagnostic message.
     */
    public NotInKubernetesException() {
        super("Application is not running inside a Kubernetes cluster");
    }
}
