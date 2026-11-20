# Stage 1: Build the Spring Boot application
FROM gradle:8-jdk21 AS build

WORKDIR /app

COPY build.gradle* settings.gradle* gradlew ./
COPY gradle ./gradle
COPY src ./src

# Fix Windows CRLF line endings and Linux executable permissions
RUN sed -i 's/\r$//' gradlew \
    && chmod +x gradlew \
    && ./gradlew bootJar --no-daemon

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy only the executable Spring Boot JAR
COPY --from=build /app/build/libs/ app-libs/

RUN find app-libs -maxdepth 1 -name '*.jar' ! -name '*-plain.jar' -exec cp {} app.jar \;

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]