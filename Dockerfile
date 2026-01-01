# Stage 1: Build React Frontend
FROM node:20-alpine AS frontend-build
WORKDIR /app
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# Stage 2: Build Spring Boot Backend
FROM maven:3.9.6-eclipse-temurin-21-alpine AS backend-build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Copy React build to Spring Boot static resources so it is served at /
COPY --from=frontend-build /app/dist ./src/main/resources/static
RUN mvn clean package -DskipTests

# Stage 3: Final Runtime Image
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend-build /app/target/log-bot-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9090
ENV OPENAI_API_KEY=""
# Install required libraries for ONNX Runtime (used by embedding model)
# Standard JRE image (Ubuntu/Debian based) usually has glibc, which ONNX Runtime needs.
ENTRYPOINT ["java", "-jar", "app.jar"]
