FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY . .

# FIX 5: Ensure setup scripts are executable
RUN chmod +x /app/infrastructure/setup-env.sh

# Build the application
RUN ./gradlew build -x test

EXPOSE 8080

CMD ["java", "-jar", "build/libs/backend-1.0.0.jar"]
