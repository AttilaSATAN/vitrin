package com.tusas.vitrin.infrastructure.adapter.output.kubernetes;

import com.tusas.vitrin.application.port.output.KubernetesPort;
import com.tusas.vitrin.domain.exception.NotInKubernetesException;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.util.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Kubernetes output adapter that creates producer pods via the in-cluster Kubernetes API.
 * Detects the cluster environment by checking the {@code KUBERNETES_SERVICE_HOST} variable
 * injected by Kubernetes into every pod — throws {@link NotInKubernetesException} if absent.
 */
@Component
public class KubernetesAdapter implements KubernetesPort {

    private final String namespace;
    private final String producerImage;

    /**
     * Constructs a KubernetesAdapter with configurable namespace and producer image.
     *
     * @param namespace     the Kubernetes namespace in which to create producer pods
     * @param producerImage the container image used for the producer pod
     */
    public KubernetesAdapter(
            @Value("${producer.namespace:default}") String namespace,
            @Value("${producer.image:tusas-vitrin-producer:latest}") String producerImage) {
        this.namespace = namespace;
        this.producerImage = producerImage;
    }

    /**
     * Creates a one-shot producer pod in the configured Kubernetes namespace.
     * Environment variables for the PostgreSQL connection are forwarded from the
     * backend pod's own environment so the producer can reach the same database.
     *
     * @return the name Kubernetes assigned to the created pod
     * @throws NotInKubernetesException if {@code KUBERNETES_SERVICE_HOST} is not set
     * @throws RuntimeException         if the Kubernetes API call fails
     */
    @Override
    public String createProducerPod() {
        if (System.getenv("KUBERNETES_SERVICE_HOST") == null) {
            throw new NotInKubernetesException();
        }

        try {
            ApiClient client = Config.fromCluster();
            CoreV1Api api = new CoreV1Api(client);

            V1Pod pod = new V1Pod()
                    .metadata(new V1ObjectMeta()
                            .generateName("tusas-producer-")
                            .namespace(namespace))
                    .spec(new V1PodSpec()
                            .restartPolicy("Never")
                            // Explicitly null out overhead and runtimeClassName so the client-java
                            // library does not serialize empty defaults that trigger the admission
                            // controller: "Pod Overhead set without corresponding RuntimeClass".
                            .overhead(null)
                            .runtimeClassName(null)
                            .addContainersItem(new V1Container()
                                    .name("producer")
                                    .image(producerImage)
                                    .imagePullPolicy("Never")
                                    .env(buildEnvVars())));

            V1Pod created = api.createNamespacedPod(namespace, pod).execute();
            return created.getMetadata().getName();

        } catch (IOException e) {
            throw new RuntimeException("Failed to load in-cluster Kubernetes config: " + e.getMessage(), e);
        } catch (ApiException e) {
            throw new RuntimeException(
                    "Kubernetes API error (" + e.getCode() + "): " + e.getResponseBody(), e);
        }
    }

    /**
     * Builds the list of environment variables to inject into the producer pod,
     * forwarding the PostgreSQL connection details from the backend's own environment.
     *
     * @return list of Kubernetes EnvVar objects
     */
    private List<V1EnvVar> buildEnvVars() {
        return List.of(

                envVar("API_URL")
        );
    }

    /**
     * Creates a Kubernetes EnvVar sourced from the current process environment.
     *
     * @param name the environment variable name
     * @return a V1EnvVar with the same name and value
     */
    private V1EnvVar envVar(String name) {
        String value = System.getenv(name);
        return new V1EnvVar().name(name).value(value != null ? value : "");
    }
}
