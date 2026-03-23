# Use OpenJDK as the base image
FROM eclipse-temurin:21-jdk

# Set working directory
WORKDIR /app

# Copy the application JAR file

# To build the ODS platform environment
COPY core/target/core-0.0.1-SNAPSHOT.jar app.jar

# Run the application
CMD ["java", "-jar", "app.jar"]
