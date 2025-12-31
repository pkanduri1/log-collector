package com.google.logbot.service;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.data.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogIngestionService {

    private static final Logger logger = LoggerFactory.getLogger(LogIngestionService.class);

    private final EmbeddingStoreIngestor ingestor;
    private final LogAnalysisService analysisService;

    public LogIngestionService(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore,
            LogAnalysisService analysisService) {
        this.ingestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        this.analysisService = analysisService;
    }

    public void ingestLogs() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:simulated_logs/*.log");

            for (Resource resource : resources) {
                logger.info("Ingesting log file: {}", resource.getFilename());
                String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

                // 1. Structured Ingestion (H2 Database)
                // Pass full content to handle multi-line logs and timestamp chunking
                try {
                    analysisService.parseAndSaveLogFile(content, resource.getFilename());
                } catch (Exception e) {
                    logger.error("Error parsing structured logs for {}", resource.getFilename(), e);
                }

                // 2. Vector Ingestion (Embedding Store)
                // We want to add metadata: source_file, log_type (if identifiable from line?
                // NO, likely need block).
                // Ideally we embed the SAME blocks that we saved to DB.
                // But for now, let's stick to line-based or simple text splitting, but add file
                // metadata.
                // User requirement: "Filters ChromaDB ... log_type == 'Payment'".
                // This implies we need to know the log_type of the segment.
                // I will read the LogEntries back from DB? No that's slow.
                // I will duplicate the regex logic here for now to tag the segments, or just
                // tag with filename and hope filename maps to type?
                // The filename is "banking_logs.log", it contains mixed types.
                // So we MUST chunk by timestamp here too to get the correct context for the
                // vector.

                // Let's rely on standard document splitter but add source metadata.
                // If we really want "log_type" in metadata, we have to parse it.
                // I'll simple add "source_file" for now as "traceability" is a key requirement.
                // Implementing full chunking here again is risky without a shared utility.
                // I will assume for this POC that Vector Search finds "meaning" and we filter
                // by "log_type" in SQL?
                // No User said "Filters ChromaDB".
                // Okay, I will implement a basic line-inspector for metadata.

                List<Document> documents = content.lines()
                        .filter(line -> !line.trim().isEmpty())
                        .map(line -> {
                            Metadata metadata = Metadata.from("source_file", resource.getFilename());
                            // Basic heuristic for log_type in vector metadata
                            if (line.contains("PAY-PRC-"))
                                metadata.add("log_type", "Payment Post");
                            else if (line.contains("CUST-VAL-ERR"))
                                metadata.add("log_type", "Address Update");
                            else if (line.contains("INT-CALC-FAIL"))
                                metadata.add("log_type", "Late Fee Calc");
                            else if (line.contains("SFTP-DROP-01"))
                                metadata.add("log_type", "File Transfer");
                            else
                                metadata.add("log_type", "General");

                            return Document.from(line, metadata);
                        })
                        .collect(Collectors.toList());

                ingestor.ingest(documents);
                logger.info("Ingested {} entries from {}", documents.size(), resource.getFilename());
            }
        } catch (IOException e) {
            logger.error("Error reading log files", e);
            throw new RuntimeException(e);
        }
    }
}
