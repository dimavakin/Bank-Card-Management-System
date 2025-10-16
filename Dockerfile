FROM maven:3.9.9-amazoncorretto-21 AS builder

WORKDIR /workspace
COPY . .

RUN mvn clean package -DskipTests

FROM amazoncorretto:21-alpine
WORKDIR /app

COPY --from=builder /workspace/target/bank_rest-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]