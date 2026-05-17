package com.tusas.vitrin.infrastructure.adapter.input.rest;

import com.tusas.vitrin.application.port.input.ProducerUseCase;
import com.tusas.vitrin.domain.exception.NotInKubernetesException;
import com.tusas.vitrin.infrastructure.adapter.input.rest.dto.ProducerSpawnResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter exposing producer lifecycle operations under /api/producer.
 */
@RestController
@RequestMapping("/api/producer")
public class ProducerController {

    private final ProducerUseCase producerUseCase;

    /**
     * Constructs a ProducerController with the required use case.
     *
     * @param producerUseCase the application-level producer use case
     */
    public ProducerController(ProducerUseCase producerUseCase) {
        this.producerUseCase = producerUseCase;
    }

    /**
     * Spawns a new producer pod in the Kubernetes cluster.
     * Returns 201 Created with the pod name on success.
     * Returns 503 Service Unavailable when not running in Kubernetes.
     *
     * @return the spawn result containing pod name, status, and message
     */
    @PostMapping("/spawn")
    public ResponseEntity<ProducerSpawnResponse> spawnProducer() {
        String podName = producerUseCase.spawnProducer();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ProducerSpawnResponse(podName, "CREATED", "Producer pod created successfully"));
    }

    /**
     * Handles the case where the application is not running inside a Kubernetes cluster.
     *
     * @param ex the exception thrown by the Kubernetes adapter
     * @return 503 Service Unavailable with an explanatory message
     */
    @ExceptionHandler(NotInKubernetesException.class)
    public ResponseEntity<ProducerSpawnResponse> handleNotInKubernetes(NotInKubernetesException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ProducerSpawnResponse(null, "UNAVAILABLE", ex.getMessage()));
    }
}
