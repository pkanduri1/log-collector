# Upgrade Guide: Java 21 & Spring Boot

This document provides the steps to upgrade `log-bot` to **Java 21 (LTS)** and the latest available **Spring Boot** version.

> **Note on Spring Boot 4**: As of current timelines, Spring Boot 4 is not yet generally available. The latest stable major version is **Spring Boot 3.4.x**. This guide targets Spring Boot 3.4+, which offers full support for Java 21 and virtual threads.

## 1. Upgrade to Java 21

Java 21 is a Long-Term Support (LTS) release that introduces **Virtual Threads** (Project Loom), which significantly improves scalability for I/O-heavy applications like this Log Bot.

### Step 1: Install Java 21
Ensure you have the JDK 21 installed on your build machine/server.
```bash
java -version
# Should output: openjdk version "21.x.x" ...
```

### Step 2: Update `pom.xml` Properties
Update the java version property in your `pom.xml`:

```xml
<properties>
    <java.version>21</java.version>
</properties>
```

## 2. Upgrade Spring Boot Version

To leverage Java 21 properly (especially Virtual Threads), you should use Spring Boot 3.2 or later.

### Step 1: Update Parent POM
Change the parent version to the latest stable release (e.g., 3.4.1):

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.4.1</version> <!-- Update from 3.2.0 -->
    <relativePath/>
</parent>
```

## 3. Enable Virtual Threads (Performance Boost)

Spring Boot 3.2+ running on Java 21 can enable Virtual Threads to handle thousands of concurrent log ingestion requests with minimal overhead.

### Step 1: Add Configuration
Add this line to `src/main/resources/application.properties`:

```properties
spring.threads.virtual.enabled=true
```

## 4. Verification

After updating, clean and rebuild the application:

```bash
mvn clean package
java -jar target/log-bot-0.0.1-SNAPSHOT.jar
```

### Checklist
- [ ] Application starts without errors.
- [ ] `java -version` confirms 21.
- [ ] Logs show Tomcat starting with virtual threads (if enabled).

## Why Upgrade?

| Feature | Benefit for Log Bot |
| :--- | :--- |
| **Virtual Threads** | High-throughput log ingestion without blocking OS threads. |
| **Generational ZGC** | Lower latency GC pauses, ensuring smoother AI interactions. |
| **Pattern Matching** | Cleaner code in `LogAnalysisService` (e.g., `switch` expressions). |
