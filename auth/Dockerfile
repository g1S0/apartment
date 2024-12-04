FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY target/auth-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]