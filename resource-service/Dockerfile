FROM eclipse-temurin:21-jdk-alpine-3.21

RUN addgroup -S zero && adduser -S zero -G zero
USER zero:zero

COPY target/ressource-service-*.jar ressource-service.jar
ENTRYPOINT ["java","-jar","/ressource-service.jar"]