# Secure Demo Environment Setup Guide

This document outlines the steps required to deploy the **AI-Powered Log Collector** in a secure environment for a Proof of Concept (POC) or Demo.

## 1. Infrastructure & Networking

To prevent unauthorized access to the Log Bot (which handles sensitive error logs), ensure the environment is isolated.

*   **Network Isolation**: Deploy the application within a virtual private cloud (VPC) or a firewalled network.
*   **Access Control**: Restrict inbound traffic (Port 9090 for Backend, 5173 for Frontend) to only trusted IP addresses (e.g., the corporate VPN or specific demo machines).
*   **Reverse Proxy with SSL**:
    *   Do **NOT** expose the Spring Boot server directly to the internet.
    *   Use **Nginx** or **Apache** as a reverse proxy to handle SSL/TLS termination (HTTPS).
    *   Generate a valid certificate (e.g., LetsEncrypt) or use a corporate wildcard certificate.

## 2. Secret Management

The application currently uses an OpenAI API Key. In a secure demo:

*   **Avoid Hardcoding**: Do not put the key in `application.properties` or commit it to Git.
*   **Environment Variables**: Inject the key at runtime.
    ```bash
    export OPENAI_API_KEY="sk-..."
    java -jar -Dopenai.api.key=$OPENAI_API_KEY log-bot-0.0.1-SNAPSHOT.jar
    ```
*   **Secret Manager**: For AWS/GCP/Azure deployments, use their respective Secret Managers to inject the value into the container environment.

## 3. Database Persistence & Security

The default setup uses H2 (In-Memory). For a robust demo:

*   **Switch to PostgreSQL**:
    *   Provision a PostgreSQL instance (RDS or managed service).
    *   Update `pom.xml` to include the `postgresql` driver.
    *   Update `application.properties` with JDBC URL, user, and password (injected via secrets).
*   **Data Encryption**: Ensure the database storage is encrypted at rest.
*   **Vector Store**: If the demo scales, migrate from the in-memory EmbeddingStore to a persistent vector DB like **ChromaDB** or **Pinecone** managed instances.

## 4. Authentication (Optional but Recommended)

The current POC has no login. For a secure demo:

*   **Basic Auth**: Add `spring-boot-starter-security` to `pom.xml` and configure a default user/password in `application.properties` to prevent random users from querying logs.
*   **API Gateway**: Place the bot behind an API Gateway (e.g., Kong, AWS API Gateway) to handle authentication (OAuth2/SSO) before requests reach the bot.

## 5. Docker Deployment

To ensure consistent and secure execution, containerize the application.

**Dockerfile (Backend)**:
```dockerfile
FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENV OPENAI_API_KEY=""
ENTRYPOINT ["java","-jar","/app.jar"]
```

**Run Command**:
```bash
docker run -d -p 9090:9090 -e OPENAI_API_KEY="sk-my-secret-key" my-log-bot
```

## 6. Audit Logging

For banking compliance demos:
*   Enable **Access Logs** in Spring Boot (`server.tomcat.accesslog.enabled=true`).
*   Ensure the AI's "Tool Usage" (Queries run against the DB) is logged to a separate secure audit stream to track what questions users are asking about the data.
