package dev.wittek.tcgenai;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.SocatContainer;

public class ModelRunnerContainer extends GenericContainer {

    public static final String MODEL_RUNNER_ENDPOINT = "model-runner.docker.internal";
    private SocatContainer socat;
    private String model;

    @Override
    public void start() {
        logger().debug("Pulling model: {}", model);

        socat = new SocatContainer()
                .withTarget(80, MODEL_RUNNER_ENDPOINT, 80);
        socat.start();
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
