# -------------------
# 1. Builder stage
# -------------------
FROM maven:3.9-eclipse-temurin-23 AS builder
WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

ARG SENTRY_ORG
ARG SENTRY_PROJECT

RUN rm ./src/main/resources/application-local.yaml
RUN --mount=type=secret,id=sentry_token export SENTRY_AUTH_TOKEN=$(cat /run/secrets/sentry_token) && SENTRY_ORG=${SENTRY_ORG} SENTRY_PROJECT=${SENTRY_PROJECT} mvn clean package -DskipTests -B

FROM bellsoft/liberica-runtime-container:jre-23-slim-musl AS runtime
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

RUN addgroup --system appgroup && adduser --system appuser --ingroup appgroup
USER appuser

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
