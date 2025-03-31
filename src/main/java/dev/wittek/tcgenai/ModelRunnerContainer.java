package dev.wittek.tcgenai;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.SocatContainer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ModelRunnerContainer extends GenericContainer {

    public static final String MODEL_RUNNER_ENDPOINT = "model-runner.docker.internal";
    private SocatContainer socat;
    private String model;

    @Override
    public void start() {
        socat = new SocatContainer()
                .withTarget(80, MODEL_RUNNER_ENDPOINT, 80);
        socat.start();

        logger().info("Pulling model: {}. Please be patient, no progress bar yet!", model);
        try {
            // Construct JSON payload
            String json = String.format("{\"from\":\"%s\"}", model);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + socat.getHost() + ":" + socat.getMappedPort(80) + "/models/create"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            logger().info(response.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        logger().info("Finished pulling model: {}", model);
    }

    @Override
    public void stop() {
        socat.stop();
    }

    public String getOpenAIEndpoint() {
        return "http://" + socat.getHost() + ":" + socat.getMappedPort(80) + "/engines/v1";
    }

    public ModelRunnerContainer withModel(String modelName) {
        this.model = modelName;
        return this;
    }
}
