# Stage 1: Build the application
FROM maven:3.9.8-amazoncorretto-17 AS build

# Set the working directory
WORKDIR /app

# Copy the Maven configuration files
COPY pom.xml ./
COPY src src

# Package the application. Skip tests
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 9090
# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]