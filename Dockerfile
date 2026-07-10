# Build stage: only invalidated by pom.xml/src changes, not by files outside the build context
# that .dockerignore already excludes (target/, .git, admin-panel/, etc).
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B package -DskipTests

# Runtime stage: no Maven, no source, no build cache -- just a JRE and the packaged jar.
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/incident-management-app-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
