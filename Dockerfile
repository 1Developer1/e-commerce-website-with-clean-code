# 1. Build Stage
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
# Make maven wrapper executable and download dependencies
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline

COPY src ./src
# Build the application
RUN ./mvnw clean package -DskipTests

# 2. Production Stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create a non-root user and group
RUN addgroup -S ecommercegroup && adduser -S ecommerceuser -G ecommercegroup

# Copy the built jar from builder
COPY --from=builder /app/target/clean-ecommerce-1.0-SNAPSHOT.jar app.jar

# Change ownership
RUN chown ecommerceuser:ecommercegroup app.jar

# Switch to non-root user
USER ecommerceuser

# Expose default port
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
