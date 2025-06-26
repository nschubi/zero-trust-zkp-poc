FROM eclipse-temurin:21-jdk-alpine-3.21

RUN addgroup -S zero && adduser -S zero -G zero
USER zero:zero

COPY target/pep-service-*.jar pep-service.jar
ENTRYPOINT ["java","-jar","/pep-service.jar"]