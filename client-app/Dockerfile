FROM eclipse-temurin:21-jdk-alpine-3.21

RUN addgroup -S zero && adduser -S zero -G zero
USER zero:zero

COPY target/client-app-*.jar client-app.jar
ENTRYPOINT ["java","-jar","/client-app.jar"]