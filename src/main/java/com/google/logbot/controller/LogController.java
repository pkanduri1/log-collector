package com.google.logbot.controller;

import com.google.logbot.service.LogAssistant;
import com.google.logbot.service.LogIngestionService;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

/**
 * REST Controller for the Log Analysis Bot.
 * <p>
 * Exposes endpoints for log ingestion and chat interactions.
 * Connects the Frontend UI with the Backend Services.
 * </p>
 */
@RestController
@RequestMapping("/api/logs")
@CrossOrigin(origins = "*") // Allow frontend access
public class LogController {

    private final LogIngestionService ingestionService;
    private final LogAssistant logAssistant;

    public LogController(LogIngestionService ingestionService, LogAssistant logAssistant) {
        this.ingestionService = ingestionService;
        this.logAssistant = logAssistant;
    }

    /**
     * Triggers the log ingestion process.
     * Scans the 'simulated_logs' directory and processes all supported files.
     *
     * @return Status message indicating ingestion has started.
     */
    @PostMapping("/ingest")
    public String ingestLogs() {
        // Trigger ingestion in a separate thread or blocking? Blocking for POC is fine.
        ingestionService.ingestLogs();
        return "Ingestion validation started...";
    }

    /**
     * Handles user chat queries about the logs.
     * Routines to the AI Assistant for intelligent response generation.
     *
     * @param q The user's question (e.g., "Summarize errors").
     * @return A map containing the query, the AI's response, and a count (for
     *         frontend compat).
     */
    @GetMapping("/query")
    public Map<String, Object> queryLogs(@RequestParam String q) {
        String answer = logAssistant.chat(q);
        return Map.of(
                "query", q,
                "results", Collections.singletonList(answer), // Frontend expects a list for now
                "count", 1);
    }
}
