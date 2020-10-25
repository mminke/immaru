FROM openjdk:13-jdk-alpine
RUN addgroup -S media-manager && adduser -S media-manager -G media-manager

ARG JAR_FILE=media-library-app/target/*.jar
COPY ${JAR_FILE} app.jar
COPY docker/config/application.yml application.yml

RUN mkdir /data  && chown media-manager:media-manager /data
VOLUME /data
USER media-manager:media-manager

ENTRYPOINT ["java","-jar","/app.jar"]