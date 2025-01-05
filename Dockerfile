# Use Maven with OpenJDK 17
FROM maven:3.8-openjdk-17 AS build

# Set the working directory
WORKDIR /app

# Copy the Maven project files to the container
COPY pom.xml .
COPY src ./src

# Run Maven to build the project
RUN mvn clean package

# Use a slim OpenJDK image for the final container
FROM openjdk:17-slim

# Set the working directory in the container
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/target/PlayerCompanion.jar PlayerCompanion.jar

# Expose the application port
EXPOSE 8070

# Command to run the application
ENTRYPOINT ["java", "-jar", "PlayerCompanion.jar"]
