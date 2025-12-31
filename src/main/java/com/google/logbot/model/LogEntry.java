package com.google.logbot.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "logs")
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;
    private String level;
    private String serviceName;
    private String errorCode; // Extracted error code, e.g., TXN-1001

    private String logType; // e.g., Payment_Batch, Customer_Sync
    private String sourceFile;

    @Column(length = 2000)
    private String message;

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
