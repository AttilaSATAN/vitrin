package com.tusas.vitrin.infrastructure.adapter.input.rest.dto;

/**
 * Response DTO for producer pod spawn operations.
 */
public class ProducerSpawnResponse {

    private final String podName;
    private final String status;
    private final String message;

    /**
     * Constructs a ProducerSpawnResponse.
     *
     * @param podName the name of the created pod, or null when unavailable
     * @param status  a short status token: {@code CREATED} or {@code UNAVAILABLE}
     * @param message a human-readable description of the outcome
     */
    public ProducerSpawnResponse(String podName, String status, String message) {
        this.podName = podName;
        this.status = status;
        this.message = message;
    }

    /** @return the pod name assigned by Kubernetes, or null */
    public String getPodName() { return podName; }

    /** @return short status token */
    public String getStatus() { return status; }

    /** @return human-readable outcome message */
    public String getMessage() { return message; }
}
