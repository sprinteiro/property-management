# Stage 1: Build the application
FROM eclipse-temurin:21-jdk AS build
# Set the working directory inside the builder container
WORKDIR /app

# --- Dependency Layer ---
# Copy only pom.xml files first
COPY ../../pom.xml ./pom.xml
COPY ../../property-management-business-core/pom.xml ./property-management-business-core/pom.xml
COPY ../../property-management-business-app/pom.xml ./property-management-business-app/pom.xml
COPY ../../property-management-app/pom.xml ./property-management-app/pom.xml
COPY ../../property-management-avro-schemas/pom.xml ./property-management-avro-schemas/pom.xml

# --- Project Files Layer ---
# Copy project structure and wrapper
COPY ../../mvnw ./mvnw
COPY ../../.mvn .mvn
RUN chmod +x mvnw

# Download Maven dependencies
RUN ./mvnw -B dependency:go-offline

# Copy source code
COPY ../../property-management-business-core ./property-management-business-core
COPY ../../property-management-business-app ./property-management-business-app
COPY ../../property-management-app ./property-management-app
COPY ../../property-management-avro-schemas ./property-management-avro-schemas

# Build the application and create the executable JAR
RUN ./mvnw -B clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-jammy AS runtime
RUN groupadd --gid 1000 spring && useradd --uid 1000 --gid spring spring
WORKDIR /app
RUN mkdir -p /app/log && chown -R spring:spring /app/log && chmod -R 775 /app/log
USER spring:spring
COPY --from=build /app/property-management-app/target/*.jar application.jar
ENTRYPOINT ["java", "-jar", "application.jar"]