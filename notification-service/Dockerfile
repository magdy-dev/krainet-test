# Build stage
FROM maven:3.8.7-eclipse-temurin-17 AS build

# Create the directory structure
WORKDIR /app

# Copy the root POM first
COPY pom.xml .

# Copy the krainet-common POM and source
COPY krainet-common/pom.xml ./krainet-common/

# Copy the notification-service POM
COPY notification-service/pom.xml ./notification-service/

# Download dependencies for krainet-common first
RUN mvn -B dependency:go-offline -f krainet-common/pom.xml

# Copy krainet-common source
COPY krainet-common/src ./krainet-common/src

# Build krainet-common
RUN mvn -B install -f krainet-common/pom.xml -DskipTests

# Now build notification-service with the installed krainet-common
COPY notification-service/src ./notification-service/src

# Build the notification-service
RUN mvn -B package -f notification-service/pom.xml -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/notification-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
