# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# IMPORTANT: Install libraries needed for PDF rendering/fonts
RUN apt-get update && apt-get install -y libfreetype6 libfontconfig1 libxrender1 libxtst6 libxi6 && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar

# Bind to Render's dynamic port
ENTRYPOINT ["java", "-Xmx300m", "-Xss512k", "-Djava.awt.headless=true", "-Dserver.port=${PORT:8080}", "-jar", "app.jar"]
