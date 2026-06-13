# Build stage
FROM maven:3.9.9-eclipse-temurin-25 AS builder
WORKDIR /workspace
COPY mvnw pom.xml ./
COPY .mvn .mvn
COPY src src
RUN chmod +x mvnw && ./mvnw -B -DskipTests package

# Runtime stage
FROM eclipse-temurin:25-jre-jammy AS runtime
WORKDIR /app
COPY --from=builder /workspace/target/chess-game-1.0.0-SNAPSHOT.jar /app/

# Default entrypoint: run the Swing GUI
ENTRYPOINT ["java", "-cp", "/app/chess-game-1.0.0-SNAPSHOT.jar", "com.chessgame.Main"]

# Note: GUI execution within a container requires display forwarding (X11/Wayland/VNC).
