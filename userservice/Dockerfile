FROM eclipse-temurin:21-jdk
WORKDIR /userservice
COPY target/*.jar userservice.jar
ENTRYPOINT ["java","-jar","userservice.jar"]
EXPOSE 8080