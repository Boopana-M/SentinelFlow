# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace

# Copy Maven definitions
COPY pom.xml ./
COPY gateway/pom.xml gateway/
COPY backend-service/pom.xml backend-service/

# Copy backend service source code
COPY backend-service/src backend-service/src

# Build backend microservice
RUN mvn clean package -pl backend-service -am -DskipTests

# Runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /workspace/backend-service/target/backend-service-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8081

HEALTHCHECK --interval=10s --timeout=3s --retries=3 \
  CMD wget -qO- http://localhost:8081/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
