package com.tusas.vitrin.application.port.input;

/**
 * Input port for producer lifecycle operations.
 */
public interface ProducerUseCase {

    /**
     * Spawns a new producer pod in the Kubernetes cluster.
     *
     * @return the name assigned to the created pod
     * @throws com.tusas.vitrin.domain.exception.NotInKubernetesException if the application
     *         is not running inside a Kubernetes cluster
     */
    String spawnProducer();
}
