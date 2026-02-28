# Stage 1: Build stage for web parts using NPM
FROM node:25.6 AS html
WORKDIR /site

# Copy the
COPY boardgame-display/ ./

# Build with npm
RUN --mount=type=cache,target=/root/.npm npm install
RUN npm run build

# Stage 2: Build stage for Java parts using Maven
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
COPY --from=html site/dist/boardgame-display.html boardgame-mcp-app/src/main/resources/content/boardgame-display.html
# Build the entire multi-module project
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw clean package -DskipTests -Dspring-boot.build-image.skip=true

# Extract layers from the Spring Boot JAR in the boardgame-mcp-app module
RUN java -Djarmode=tools -jar boardgame-mcp-app/target/*.jar extract --layers --launcher --destination extracted

# Stage 3: Runtime stage (using "latest" tag as that is the only free tag available from Chainguard)
#          Could use dhi.io/eclipse-temurin:21-alpine3.23 instead, but that requires Docker account login (free)
FROM cgr.dev/chainguard/jre:latest AS runtime

# Set working directory and expose port
WORKDIR /app
EXPOSE 8080

# Copy layers in order of least to most likely to change
# This optimizes Docker layer caching
COPY --from=builder app/extracted/dependencies/ ./
COPY --from=builder app/extracted/spring-boot-loader/ ./
COPY --from=builder app/extracted/snapshot-dependencies/ ./
COPY --from=builder app/extracted/application/ ./

# Copy SBOM to the application root for easier descovery for scanners
COPY --from=builder app/extracted/application/META-INF/sbom/application.cdx.json ./sbom.json

# Set JVM options for containerized environment
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport -XX:+ExitOnOutOfMemoryError"

# Run the application
CMD ["org.springframework.boot.loader.launch.JarLauncher"]
