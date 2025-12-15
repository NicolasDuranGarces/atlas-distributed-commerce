FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="Atlas Commerce Team"
LABEL description="API Gateway"

RUN addgroup -g 1001 atlas && adduser -u 1001 -G atlas -s /bin/sh -D atlas

WORKDIR /app

COPY target/*.jar app.jar

RUN chown -R atlas:atlas /app

USER atlas

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseContainerSupport"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
