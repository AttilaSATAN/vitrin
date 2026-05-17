package com.tusas.vitrin.application.service;

import com.tusas.vitrin.application.port.input.ProducerUseCase;
import com.tusas.vitrin.application.port.output.KubernetesPort;
import org.springframework.stereotype.Service;

/**
 * Application service that orchestrates producer pod lifecycle.
 * Delegates cluster operations to the {@link KubernetesPort} output port.
 */
@Service
public class ProducerService implements ProducerUseCase {

    private final KubernetesPort kubernetesPort;

    /**
     * Constructs a ProducerService with the required Kubernetes output port.
     *
     * @param kubernetesPort the Kubernetes infrastructure adapter
     */
    public ProducerService(KubernetesPort kubernetesPort) {
        this.kubernetesPort = kubernetesPort;
    }

    /**
     * Delegates producer pod creation to the Kubernetes output port.
     *
     * @return the name of the created pod
     */
    @Override
    public String spawnProducer() {
        return kubernetesPort.createProducerPod();
    }
}
