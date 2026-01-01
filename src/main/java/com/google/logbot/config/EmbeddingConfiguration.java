package com.google.logbot.config;

import com.google.logbot.service.LogAnalysisTools;
import com.google.logbot.service.LogAssistant;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration class for LangChain4j components.
 * <p>
 * This class configures the core AI components including the Embedding Model,
 * Vector Store, Chat Language Model, and the AI Service (LogAssistant).
 * It sets up a RAG (Retrieval-Augmented Generation) pipeline using an
 * in-memory vector store and an ONNX-based embedding model.
 * </p>
 */
@Configuration
public class EmbeddingConfiguration {

    @Value("${langchain4j.open-ai.chat-model.api-key:demo}")
    private String openAiApiKey;

    /**
     * Creates an Embedding Model bean using the AllMiniLmL6V2 ONNX model.
     * This model runs locally in the JVM and does not require an external API.
     *
     * @return The configured {@link EmbeddingModel}.
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Value("${chroma.url:http://localhost:8000}")
    private String chromaUrl;

    /**
     * Creates a ChromaDB Embedding Store.
     * Used to store vector embeddings of log entries for persistent semantic
     * search.
     *
     * @return The {@link EmbeddingStore} for {@link TextSegment}s.
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return ChromaEmbeddingStore.builder()
                .baseUrl(chromaUrl)
                .collectionName("log-embeddings")
                .timeout(Duration.ofSeconds(15))
                .build();
    }

    /**
     * Creates the LogAssistant AI Service bean.
     * This service acts as the high-level interface for interacting with the AI.
     * It connects the Chat Model, RAG Retriever, and Tools.
     *
     * @param chatLanguageModel The LLM to use for chat.
     * @param embeddingStore    The store containing log embeddings.
     * @param embeddingModel    The model used to embed queries.
     * @param logAnalysisTools  The tools available to the AI (SQL, etc.).
     * @return A proxy instance of the {@link LogAssistant} interface.
     */
    @Bean
    public LogAssistant logAssistant(ChatLanguageModel chatLanguageModel,
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel,
            LogAnalysisTools logAnalysisTools) {

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(20) // Increased to allow better analysis of multiple errors
                .minScore(0.6)
                .build();

        return AiServices.builder(LogAssistant.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(contentRetriever)
                .tools(logAnalysisTools)
                .build();
    }

    /**
     * Creates a Chat Language Model bean using OpenAI.
     *
     * @return The configured {@link OpenAiChatModel}.
     */
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName("gpt-4o-mini")
                .build();
    }
}
