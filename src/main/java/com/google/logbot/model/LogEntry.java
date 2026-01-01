package com.google.logbot.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a structured log entry.
 * <p>
 * This class maps to the "logs" table in the database and stores
 * both standard log attributes (timestamp, level) and enriched metadata
 * (error code, log type) derived during ingestion.
 * </p>
 */
@Entity
@Table(name = "logs")
public class LogEntry {

    /** Unique ID of the log entry. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;
    private String level;
    private String serviceName;

    /** Extracted error code (e.g., TXN-1001), used for grouping. */
    private String errorCode;

    /** Logical classification of the log (e.g., Payment_Batch). */
    private String logType;

    /** The original filename from which this log was ingested. */
    private String sourceFile;

    /** The primary log message or summary. */
    @Column(length = 2000)
    private String message;

    /** The full raw content of the log, including stack traces. */
    @Column(length = 5000)
    private String fullLog;

    public LogEntry() {
    }

    public LogEntry(LocalDateTime timestamp, String level, String serviceName, String errorCode, String message,
            String fullLog, String logType, String sourceFile) {
        this.timestamp = timestamp;
        this.level = level;
        this.serviceName = serviceName;
        this.errorCode = errorCode;
        this.message = message;
        this.fullLog = fullLog;
        this.logType = logType;
        this.sourceFile = sourceFile;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getLevel() {
        return level;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public String getFullLog() {
        return fullLog;
    }

    public String getLogType() {
        return logType;
    }

    public String getSourceFile() {
        return sourceFile;
    }
}
