# Upgrade Guide: Java 21 & Spring Boot 4

This document provides the steps to upgrade `log-bot` to **Java 21 (LTS)** and **Spring Boot 4**.

## 1. Upgrade to Java 21

### Step 1: Install Java 21
Ensure you have the JDK 21 installed on your build machine/server.

### Step 2: Update `pom.xml` Properties
Update the java version property in your `pom.xml`:

```xml
<properties>
    <java.version>21</java.version>
</properties>
```

## 2. Upgrade Spring Boot Version

To use Spring Boot 4 (now available as **4.0.1**):

### Step 1: Update Parent POM
Change the parent version to **4.0.1**:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.1</version>
    <relativePath/>
</parent>
```

## 3. Enable Virtual Threads

Spring Boot 4 runs on Java 21 by default and has first-class support for Virtual Threads.

### Configuration
Add this to `src/main/resources/application.properties`:

```properties
spring.threads.virtual.enabled=true
```

## 4. Migration Notes

*   **Javax to Jakarta**: Ensure you are already using `jakarta.*` packages (standard since Spring Boot 3).
*   **Deprecations**: Check the Spring Boot 4 Release Notes for any removed APIs.
*   **Security**: If using Spring Security, ensure you update to the corresponding major version compatible with Boot 4.

## 5. Verification

```bash
mvn clean package
java -jar target/log-bot-0.0.1-SNAPSHOT.jar
```
