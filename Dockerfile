FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -q 2>/dev/null || true

COPY src ./src
RUN mvn package -DskipTests -q \
 && JAR_FILE=$(find target -maxdepth 1 -type f -name "*.jar" ! -name "*.jar.original" -print -quit) \
 && test -n "$JAR_FILE" \
 && cp "$JAR_FILE" /app/app.jar

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S scheduling-service && adduser -S scheduling-service -G scheduling-service
USER scheduling-service

COPY --from=build /app/app.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "app.jar"]