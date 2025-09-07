# ---- Build stage ----
FROM maven:3.8.4-openjdk-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# ---- Runtime stage ----
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=build /app/target/research.assistant-0.0.1-SNAPSHOT.jar .

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/research.assistant-0.0.1-SNAPSHOT.jar"]
