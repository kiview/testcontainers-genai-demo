package dev.wittek.tcgenai;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.NotFoundException;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;

public class TestcontainersWithOllama {

    public static void main(String[] args) throws IOException, InterruptedException {

        // Pull model into container
        String modelName = "smollm2";
        String imageName = "ollama-" + modelName;

        // Check if the model is already pulled and committed into an image
        DockerClient client = DockerClientFactory.instance().client();
        boolean alreadyCreated;
        try {
            client.inspectImageCmd(imageName).exec();
            alreadyCreated = true;
            System.out.println("Container image " + imageName + " already exists.");
        } catch (NotFoundException e) {
            alreadyCreated = false;
        }

        if (!alreadyCreated) {
            try (OllamaContainer ollama = new OllamaContainer("ollama/ollama:0.6.3");) {
                ollama.start();

                ollama.execInContainer("ollama", "pull", modelName);
                ollama.commitToImage(imageName);
            }
        }

        // Run model in container
        OllamaContainer ollama = new OllamaContainer(
                DockerImageName.parse(imageName)
                        .asCompatibleSubstituteFor("ollama/ollama")
        );
        ollama.start();


        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl(ollama.getEndpoint() + "/v1/")
                .modelName(modelName)
                .logRequests(true)
                .logResponses(true)
                .build();

        String answer = model.chat("Give me a fact about the Nürburgring.");
        System.out.println(answer);
    }

}
