package dev.wittek.tcgenai;

import dev.langchain4j.model.openai.OpenAiChatModel;

public class TestcontainersWithModelRunner {

    public static void main(String[] args) {

        String modelName = "ai/qwen2.5";
        ModelRunnerContainer modelRunnerContainer = new ModelRunnerContainer()
                .withModel(modelName);
        modelRunnerContainer.start();

        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl(modelRunnerContainer.getOpenAIEndpoint())
                .modelName(modelName)
                .logRequests(true)
                .logResponses(true)
                .build();

        String answer = model.chat("Give me a fact about the Nürburgring.");
        System.out.println(answer);
    }

}
