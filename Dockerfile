# Stage 1: Build stage using Maven
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and parent POM
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Copy all other POMs
COPY boardgame-client/pom.xml boardgame-client/pom.xml
COPY boardgame-client/boardgame-client-core/pom.xml boardgame-client/boardgame-client-core/pom.xml
COPY boardgame-client/boardgame-client-bgg/pom.xml boardgame-client/boardgame-client-bgg/pom.xml
COPY boardgame-mcp-app/pom.xml boardgame-mcp-app/pom.xml

# Download dependencies (this layer will be cached unless any pom.xml above changes)
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw dependency:go-offline -B

# Copy all source code for all modules
COPY boardgame-client/ boardgame-client/
COPY boardgame-mcp-app/ boardgame-mcp-app/

# Build the entire multi-module project
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw clean package -DskipTests -Dspring-boot.build-image.skip=true

# Extract layers from the Spring Boot JAR in the boardgame-mcp-app module
RUN java -Djarmode=layertools -jar boardgame-mcp-app/target/*.jar extract --destination extracted

# Stage 2: Runtime stage
FROM cgr.dev/chainguard/jre:latest AS runtime

# Set working directory
WORKDIR /app

# Copy layers in order of least to most likely to change
# This optimizes Docker layer caching
COPY --from=builder app/extracted/dependencies/ ./
COPY --from=builder app/extracted/spring-boot-loader/ ./
COPY --from=builder app/extracted/snapshot-dependencies/ ./
COPY --from=builder app/extracted/application/ ./

# Set JVM options for containerized environment
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport -XX:+ExitOnOutOfMemoryError"

# Run the application
CMD ["org.springframework.boot.loader.launch.JarLauncher"]
