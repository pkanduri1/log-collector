package com.google.logbot.service;

import com.google.logbot.model.LogEntry;
import com.google.logbot.repository.LogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for parsing and analyzing standard log files.
 * <p>
 * Handles parsing of timestamp-header logs, chunking multi-line stack traces,
 * and saving structured data to the LogRepository.
 * </p>
 */
@Service
public class LogAnalysisService {

    private final LogRepository logRepository;

    // Pattern: Date Time Level [Service] [ErrorCode] Message
    // Example: 2023-10-27 10:15:30.123 ERROR [TransactionService] [TXN-1001]
    // Transaction failed...
    // Regex for start of a log line (Timestamp)
    private static final Pattern LOG_START_PATTERN = Pattern
            .compile("^(\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\.\\d{3})");

    // Updated Main Pattern to be more flexible since we now process full multi-line
    // blocks
    private static final Pattern LOG_HEADER_PATTERN = Pattern.compile(
            "^(\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\.\\d{3})\\s+(\\w+)\\s+\\[(.*?)\\]\\s+(?:\\[(.*?)\\])?");

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public LogAnalysisService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    /**
     * Parses a raw log file content and saves structured entries to the database.
     * Chunks the content by timestamp to handle multi-line error stacks.
     *
     * @param content  The full string content of the log file.
     * @param filename The name of the file (metadata).
     */
    public void parseAndSaveLogFile(String content, String filename) {
        List<String> logBlocks = chunkLogsByTimestamp(content);

        for (String block : logBlocks) {
            parseAndSaveSingleBlock(block, filename);
        }
    }

    private List<String> chunkLogsByTimestamp(String content) {
        List<String> blocks = new ArrayList<>();
        StringBuilder currentBlock = new StringBuilder();

        for (String line : content.lines().toList()) {
            Matcher matcher = LOG_START_PATTERN.matcher(line);
            if (matcher.find()) {
                // New log entry detected
                if (currentBlock.length() > 0) {
                    blocks.add(currentBlock.toString());
                    currentBlock.setLength(0); // Reset
                }
            }
            if (currentBlock.length() > 0) {
                currentBlock.append("\n");
            }
            currentBlock.append(line);
        }
        // Add last block
        if (currentBlock.length() > 0) {
            blocks.add(currentBlock.toString());
        }
        return blocks;
    }

    private void parseAndSaveSingleBlock(String logBlock, String filename) {
        // Use the first line for header parsing
        String firstLine = logBlock.split("\n")[0];
        Matcher matcher = LOG_HEADER_PATTERN.matcher(firstLine);

        if (matcher.find()) {
            try {
                String timestampStr = matcher.group(1);
                String level = matcher.group(2);
                String service = matcher.group(3);
                String errorCode = matcher.group(4); // May be null

                LocalDateTime timestamp = LocalDateTime.parse(timestampStr, DATE_FORMATTER);

                // Determine Log Type
                String logType = determineLogType(logBlock, errorCode);

                LogEntry entry = new LogEntry(timestamp, level, service, errorCode, null, logBlock, logType, filename);
                // Message is the rest of the block (simplification: we might want to strip
                // header)
                logRepository.save(entry);

            } catch (Exception e) {
                System.err.println("Failed to parse log block: " + firstLine + " -> " + e.getMessage());
            }
        }
    }

    private String determineLogType(String logBlock, String errorCode) {
        if (logBlock.contains("PAY-PRC-"))
            return "Payment Post";
        if (logBlock.contains("CUST-VAL-ERR"))
            return "Address Update";
        if (logBlock.contains("INT-CALC-FAIL"))
            return "Late Fee Calc";
        if (logBlock.contains("SFTP-DROP-01"))
            return "File Transfer";
        return "General";
    }
}
