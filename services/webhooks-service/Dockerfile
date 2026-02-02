# Converted from: src/Webhooks.API/Dockerfile
# Simple Dockerfile - copies pre-built JAR

FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 9104

ENTRYPOINT ["java", "-jar", "app.jar"]
