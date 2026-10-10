# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace

# Copy Maven definitions first for optimal layer caching
COPY pom.xml ./
COPY gateway/pom.xml gateway/
COPY backend-service/pom.xml backend-service/

# Copy gateway source code
COPY gateway/src gateway/src

# Build gateway service
RUN mvn clean package -pl gateway -am -DskipTests

# Runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /workspace/gateway/target/gateway-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=3s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
