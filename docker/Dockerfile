FROM openjdk:13-jdk-alpine

RUN addgroup -S immaru && adduser -S immaru -G immaru
RUN mkdir /data  && chown immaru:immaru /data
VOLUME /data

ARG JAR_FILE=immaru-app/target/*.jar
COPY ${JAR_FILE} app.jar
COPY docker/config/application.yml application.yml

USER immaru:immaru

ENTRYPOINT ["java","-jar","/app.jar"]