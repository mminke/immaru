FROM openjdk:13-jdk-alpine

RUN addgroup -S immaru && adduser -S immaru -G immaru
RUN mkdir /data  && chown immaru:immaru /data
VOLUME /data

USER immaru:immaru
