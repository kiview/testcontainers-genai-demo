package dev.wittek.tcgenai;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

public class RagExample {

    public static void main(String[] args) {

        var image = DockerImageName.parse("pgvector/pgvector:pg16")
                .asCompatibleSubstituteFor("postgres");
        var postgreSQLContainer = new PostgreSQLContainer<>(image);
        postgreSQLContainer.start();

        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

        List<Document> documents = FileSystemDocumentLoader.loadDocuments("src/main/resources");
        EmbeddingStore<TextSegment> embeddingStore = PgVectorEmbeddingStore.builder()
                .host(postgreSQLContainer.getHost())
                .port(postgreSQLContainer.getFirstMappedPort())
                .database(postgreSQLContainer.getDatabaseName())
                .user(postgreSQLContainer.getUsername())
                .password(postgreSQLContainer.getPassword())
                .table("test")
                .dimension(embeddingModel.dimension())
                .build();
        EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build()
                .ingest(documents);

        String modelName = "ai/qwen2.5";
        ModelRunnerContainer modelRunnerContainer = new ModelRunnerContainer()
                .withModel(modelName);
        modelRunnerContainer.start();

        ChatLanguageModel chatModel = OpenAiChatModel.builder()
                .baseUrl(modelRunnerContainer.getOpenAIEndpoint())
                .modelName(modelName)
                .logRequests(true)
                .build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(EmbeddingStoreContentRetriever.builder()
                        .embeddingStore(embeddingStore)
                        .embeddingModel(embeddingModel)
                        .build())
                .build();

        String answer = assistant.chat("Who created this repository??");
        System.out.println(answer);

        answer = assistant.chat("Under which License is this repository?");
        System.out.println(answer);

    }

    static interface Assistant {
        String chat(String userMessage);
    }

}
