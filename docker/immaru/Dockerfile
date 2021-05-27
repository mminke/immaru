FROM immaru-base:latest

ARG JAR_FILE=immaru-app/target/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","/app.jar"]