# ==========================================
# Stage 1: Build React 19 Frontend
# ==========================================
FROM node:20-alpine AS frontend-builder
WORKDIR /app/frontend

COPY frontend/package*.json ./
RUN npm install

COPY frontend/ ./
RUN npm run build

# ==========================================
# Stage 2: Build Spring Boot 3 Backend
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS backend-builder
WORKDIR /app/backend

COPY backend/pom.xml backend/mvnw ./
COPY backend/.mvn ./.mvn
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

COPY backend/src ./src

# Copy built frontend assets directly into Spring Boot static resources
COPY --from=frontend-builder /app/frontend/dist ./src/main/resources/static

RUN ./mvnw clean package -DskipTests

# ==========================================
# Stage 3: Lightweight Production JRE Container
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=backend-builder /app/backend/target/job-recruitment-backend-1.0.0.jar app.jar

EXPOSE 8080
ENV SERVER_PORT=8080

ENTRYPOINT ["java", "-jar", "app.jar"]
