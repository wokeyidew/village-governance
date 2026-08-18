FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/village-governance-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]