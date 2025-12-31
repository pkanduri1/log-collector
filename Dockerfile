# Start with a lightweight Java 21 Runtime (Compatible with Mac M1/M2/M3 & Intel)
FROM eclipse-temurin:21-jre-alpine

# Set working directory inside the container
WORKDIR /app

# Copy the built artifact from your local machine
# detailed-note: Run 'mvn clean package -DskipTests' locally before building this image
COPY target/log-bot-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 9090

# Set default environment variable (override this when running)
ENV OPENAI_API_KEY=""

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
