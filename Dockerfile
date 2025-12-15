FROM maven:3-eclipse-temurin-25-alpine AS build
WORKDIR /app
COPY . .
RUN sh build-css.sh --minify
RUN mvn -q clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
