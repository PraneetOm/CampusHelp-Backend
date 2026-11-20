# Stage 1: Build the JAR
FROM gradle:8-jdk21 AS build
WORKDIR /app
COPY build.gradle* settings.gradle* gradlew ./
COPY gradle ./gradle
COPY src ./src
RUN ./gradlew bootJar --no-daemon

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]