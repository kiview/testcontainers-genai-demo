package dev.wittek.tcgenai;

import dev.langchain4j.model.openai.OpenAiChatModel;

public class DockerModelRunner {

    public static void main(String[] args) {
        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl("http://localhost:12434/engines/v1")
                .modelName("ai/qwen2.5")
                .logRequests(true)
                .logResponses(true)
                .build();

        String answer = model.chat("Give me a fact about the Nürburgring.");
        System.out.println(answer);
    }

}
