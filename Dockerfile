# Step 1: Use the official OpenJDK image as the base image
FROM openjdk:17-jdk-alpine

# Step 2: Set the working directory in the container
WORKDIR /app

# Step 3: Copy the jar file into the container
COPY target/SecurityService-0.0.1-SNAPSHOT.jar app.jar

# Step 4: Expose the port your Spring Boot app uses
EXPOSE 8080

# Step 5: Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
