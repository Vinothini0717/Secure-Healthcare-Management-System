# Java 21 runtime image
FROM eclipse-temurin:21-jre

# Application working directory
WORKDIR /app

# Copy Spring Boot JAR file
COPY target/*.jar app.jar

# Application port
EXPOSE 8080

# Start Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]