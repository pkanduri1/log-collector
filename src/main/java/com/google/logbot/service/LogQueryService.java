package com.google.logbot.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Semantic Search Service.
 * <p>
 * Manages direct interaction with the Embedding Store for retrieving relevant
 * log segments based on vector similarity. Used by RAG components.
 * </p>
 */
@Service
public class LogQueryService {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    public LogQueryService(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
    }

    /**
     * Searches for log segments semantically similar to the query.
     *
     * @param query The search text.
     * @return List of matching text segments from the logs.
     */
    public List<String> search(String query) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(5)
                .build();

        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);

        return result.matches().stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.toList());
    }
}
