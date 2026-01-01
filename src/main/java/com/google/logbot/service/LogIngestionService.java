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
    private final TransactionReportParser reportParser;

    public LogIngestionService(EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore,
            LogAnalysisService analysisService, TransactionReportParser reportParser) {
        this.ingestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        this.analysisService = analysisService;
        this.reportParser = reportParser;
    }

    public void ingestLogs() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            // Load both .log and .txt files
            Resource[] resources = resolver.getResources("classpath:simulated_logs/*.*");
            // Also check root if transaction_log.txt is there
            Resource[] rootResources = resolver.getResources("classpath:transaction_log.txt");

            ingestResources(resources);
            ingestResources(rootResources);

        } catch (IOException e) {
            logger.error("Error reading log files", e);
            throw new RuntimeException(e);
        }
    }

    private void ingestResources(Resource[] resources) throws IOException {
        if (resources == null)
            return;

        for (Resource resource : resources) {
            if (!resource.exists())
                continue;

            logger.info("Ingesting file: {}", resource.getFilename());
            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            if (resource.getFilename().endsWith(".txt")) {
                // Handle Transaction Report
                List<Document> documents = reportParser.parse(content, resource.getFilename());
                if (!documents.isEmpty()) {
                    ingestor.ingest(documents);
                    logger.info("Ingested {} report entries from {}", documents.size(), resource.getFilename());
                }
            } else if (resource.getFilename().endsWith(".log")) {
                // 1. Structured Ingestion (H2 Database)
                try {
                    analysisService.parseAndSaveLogFile(content, resource.getFilename());
                } catch (Exception e) {
                    logger.error("Error parsing structured logs for {}", resource.getFilename(), e);
                }

                // 2. Vector Ingestion (Embedding Store) for Logs
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
        }
    }
}
