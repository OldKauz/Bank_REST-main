FROM openjdk:17-jdk-slim
WORKDIR /app
COPY /target/bank-rest-1.0.0.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]