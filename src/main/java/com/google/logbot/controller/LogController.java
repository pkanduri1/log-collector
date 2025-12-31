package com.google.logbot.controller;

import com.google.logbot.service.LogAssistant;
import com.google.logbot.service.LogIngestionService;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

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

    @PostMapping("/ingest")
    public String ingestLogs() {
        // Trigger ingestion in a separate thread or blocking? Blocking for POC is fine.
        ingestionService.ingestLogs();
        return "Ingestion validation started...";
    }

    @GetMapping("/query")
    public Map<String, Object> queryLogs(@RequestParam String q) {
        String answer = logAssistant.chat(q);
        return Map.of(
                "query", q,
                "results", Collections.singletonList(answer), // Frontend expects a list for now
                "count", 1);
    }
}
