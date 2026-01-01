package com.google.logbot.repository;

import com.google.logbot.model.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<LogEntry, Long> {

    // Find all logs with a specific error code
    List<LogEntry> findByErrorCode(String errorCode);

    // Count errors grouped by error code
    @Query("SELECT l.errorCode, COUNT(l) FROM LogEntry l WHERE l.level = 'ERROR' AND l.errorCode IS NOT NULL GROUP BY l.errorCode ORDER BY COUNT(l) DESC")
    List<Object[]> countErrorsByCode();

    // Find logs by level
    List<LogEntry> findByLevel(String level);

    @Query("SELECT l.errorCode, COUNT(l) FROM LogEntry l WHERE l.sourceFile = :filename AND l.level = 'ERROR' AND l.errorCode IS NOT NULL GROUP BY l.errorCode ORDER BY COUNT(l) DESC")
    List<Object[]> countErrorsByCodeAndFile(String filename);

    @Query("SELECT DISTINCT l.sourceFile FROM LogEntry l")
    List<String> findDistinctSourceFiles();
}
