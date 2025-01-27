# Stage 1: Build the application
FROM eclipse-temurin:21-jdk AS build
# Set the working directory inside the builder container
WORKDIR /app
# Copy the entire project to the container
COPY ../../mvnw ./mvnw
# Include the `.mvn` directory
COPY ../../.mvn .mvn
# Copy the rest of the project
COPY ../.. .
# Ensure mvnw is executable
RUN chmod +x mvnw
# Build the application and create the executable JAR (adjust if you have a specific final artifact name or module structure)
RUN ./mvnw -B clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-jammy AS runtime
# Create a non-root user for running the app
RUN groupadd --system spring && useradd --system -g spring spring
# Set the working directory inside the runtime container
WORKDIR /app
# Change to the non-root user
USER spring:spring
# Copy the built JAR file from the builder stage
# Replace `property-management-app/target/*.jar` with the appropriate module and final JAR path if necessary.
COPY --from=build /app/property-management-app/target/*.jar application.jar
# Run the application
ENTRYPOINT ["java", "-jar", "application.jar"]
