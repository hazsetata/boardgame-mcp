# Stage 1: Build stage using Maven
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY boardgame-mcp-app/target/*.jar application.jar

# Extract layers from the Spring Boot JAR in the boardgame-mcp-app module
RUN java -Djarmode=layertools -jar application.jar extract --destination extracted

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine AS runtime

# Create non-root user for security
RUN addgroup -g 1001 -S boardgame && \
    adduser -u 1001 -S boardgame -G boardgame

# Set working directory
WORKDIR /app

# Change ownership to boardgame user
RUN chown boardgame:boardgame /app
USER boardgame

# Copy layers in order of least to most likely to change
# This optimizes Docker layer caching
COPY --from=builder --chown=boardgame:boardgame app/extracted/dependencies/ ./
COPY --from=builder --chown=boardgame:boardgame app/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=boardgame:boardgame app/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=boardgame:boardgame app/extracted/application/ ./

# Set JVM options for containerized environment
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport -XX:+ExitOnOutOfMemoryError"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
