package com.google.logbot.service;

import com.google.logbot.model.LogEntry;
import com.google.logbot.repository.LogRepository;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TransactionReportParser {

    private final LogRepository logRepository;

    public TransactionReportParser(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    // Regex to capture the Account ID at the start of a line
    // Example: " 9900009054750 "
    private static final Pattern ACCOUNT_LINE_PATTERN = Pattern.compile("^\\s+(\\d{10,})");

    // Regex to capture Error Message lines
    // Example: " ERROR MESSAGE: 000201S EMPTY ACTIVE MASTER DATABASE "
    private static final Pattern ERROR_MESSAGE_PATTERN = Pattern.compile("ERROR MESSAGE:\\s+(.*)");

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy h:mma");

    // Regex to capture Transaction lines (New Acct, etc) if they appear on the same
    // line as account or separate
    // For now, key focus is Account ID + Error

    public List<Document> parse(String content, String filename) {
        List<Document> documents = new ArrayList<>();
        String[] lines = content.split("\n");

        String currentAccountId = "UNKNOWN";
        LocalDateTime reportTime = extractReportDate(content); // Base time for all entries in this report

        for (String line : lines) {
            // Check for Account ID
            Matcher accountMatcher = ACCOUNT_LINE_PATTERN.matcher(line);
            if (accountMatcher.find()) {
                currentAccountId = accountMatcher.group(1);
            }

            // Check for Error Message
            Matcher errorMatcher = ERROR_MESSAGE_PATTERN.matcher(line);
            if (errorMatcher.find()) {
                String fullErrorMessage = errorMatcher.group(1).trim();

                // Extract Error Code (first token usually)
                String errorCode = fullErrorMessage.split("\\s+")[0];

                String text = String.format("Account: %s | Error: %s", currentAccountId, fullErrorMessage);

                Metadata metadata = Metadata.from("source_file", filename);
                metadata.add("log_type", "Transaction Report Error");
                metadata.add("account_id", currentAccountId);
                metadata.add("error_code", errorCode);
                if (reportTime != null) {
                    metadata.add("report_date", reportTime.toString());
                }

                documents.add(Document.from(text, metadata));

                // Persist to DB for SQL Counting
                if (reportTime == null)
                    reportTime = LocalDateTime.now(); // Fallback
                LogEntry entry = new LogEntry(
                        reportTime,
                        "ERROR",
                        "TransactionService",
                        errorCode,
                        fullErrorMessage,
                        text,
                        "Transaction Report",
                        filename);
                try {
                    logRepository.save(entry);
                } catch (Exception e) {
                    // ignore duplicates or persistence errors to ensure flow continues
                }
            }
        }

        return documents;
    }

    private LocalDateTime extractReportDate(String content) {
        // RPT ID: ZT1030 DATE: 08/13/2022 TIME: 01:27P
        Pattern datePattern = Pattern.compile("DATE:\\s+(\\d{2}/\\d{2}/\\d{4})\\s+TIME:\\s+(\\d{2}:\\d{2}[AP]?)");
        Matcher matcher = datePattern.matcher(content);
        if (matcher.find()) {
            String dateStr = matcher.group(1) + " " + matcher.group(2);
            try {
                // Clean up 'P' to 'PM' if needed or rely on flexible parser, java requires PM
                // Simple hack: if length is short (01:27P), insert M
                if (dateStr.endsWith("P"))
                    dateStr += "M";
                if (dateStr.endsWith("A"))
                    dateStr += "M";
                return LocalDateTime.parse(dateStr, DATE_FORMATTER);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
