FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S whitewater && adduser -S whitewater -G whitewater
COPY --from=build /app/target/*.jar app.jar
RUN chown whitewater:whitewater app.jar \
    && mkdir -p /app/uploads \
    && chown whitewater:whitewater /app/uploads
USER whitewater

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]