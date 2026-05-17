package com.tusas.vitrin.application.port.output;

/**
 * Output port abstracting Kubernetes cluster operations.
 */
public interface KubernetesPort {

    /**
     * Creates a new producer pod in the Kubernetes cluster.
     *
     * @return the name assigned to the created pod
     */
    String createProducerPod();
}
