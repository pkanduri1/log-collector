package com.google.logbot.repository;

import com.google.logbot.model.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for accessing LogEntry data from the H2 database.
 * <p>
 * Provides standard CRUD and custom JPQL queries for log analysis.
 * </p>
 */
@Repository
public interface LogRepository extends JpaRepository<LogEntry, Long> {

    /**
     * Finds all log entries matching a specific error code.
     *
     * @param errorCode The error code to search for (e.g., "000201S").
     * @return List of matching LogEntries.
     */
    List<LogEntry> findByErrorCode(String errorCode);

    /**
     * Aggregates errors by error code and counts their occurrences.
     * Only considers entries with level 'ERROR'.
     *
     * @return A list of object arrays where [0] is errorCode (String) and [1] is
     *         count (Long).
     */
    @Query("SELECT l.errorCode, COUNT(l) FROM LogEntry l WHERE l.level = 'ERROR' AND l.errorCode IS NOT NULL GROUP BY l.errorCode ORDER BY COUNT(l) DESC")
    List<Object[]> countErrorsByCode();

    // Find logs by level
    List<LogEntry> findByLevel(String level);

    /**
     * Aggregates errors by error code for a specific source file.
     * 
     * @param filename The name of the file to filter by (e.g.,
     *                 "transaction_log.txt").
     * @return A list of object arrays where [0] is errorCode (String) and [1] is
     *         count (Long).
     */
    @Query("SELECT l.errorCode, COUNT(l) FROM LogEntry l WHERE l.sourceFile = :filename AND l.level = 'ERROR' AND l.errorCode IS NOT NULL GROUP BY l.errorCode ORDER BY COUNT(l) DESC")
    List<Object[]> countErrorsByCodeAndFile(String filename);

    /**
     * Retrieves a distinct list of all filenames that have been ingested.
     *
     * @return List of unique source filenames.
     */
    @Query("SELECT DISTINCT l.sourceFile FROM LogEntry l")
    List<String> findDistinctSourceFiles();
}
