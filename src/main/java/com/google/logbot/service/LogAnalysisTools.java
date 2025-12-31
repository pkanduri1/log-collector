package com.google.logbot.service;

import com.google.logbot.repository.LogRepository;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class LogAnalysisTools {

    private final LogRepository logRepository;

    public LogAnalysisTools(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Tool("Returns a summary of errors grouped by error code with counts")
    public String getErrorSummary() {
        List<Object[]> results = logRepository.countErrorsByCode();
        if (results.isEmpty()) {
            return "No errors found in the logs.";
        }

        StringBuilder sb = new StringBuilder("Error Summary:\n");
        for (Object[] row : results) {
            String errorCode = (String) row[0];
            Long count = (Long) row[1];
            sb.append(String.format("- %s: %d occurrences\n", errorCode, count));
        }
        return sb.toString();
    }

    @Tool("Returns detailed log messages for a specific error code")
    public String getErrorDetails(String errorCode) {
        return logRepository.findByErrorCode(errorCode).stream()
                .map(log -> String.format("[%s] %s: %s", log.getTimestamp(), log.getServiceName(), log.getMessage()))
                .collect(Collectors.joining("\n"));
    }
}
