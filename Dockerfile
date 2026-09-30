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
FROM maven:3.9.6-eclipse-temurin-21-alpine AS backend-builder
WORKDIR /app/backend

COPY backend/pom.xml ./
COPY backend/src ./src

# Copy built frontend assets directly into Spring Boot static resources
COPY --from=frontend-builder /app/frontend/dist ./src/main/resources/static

RUN mvn clean package -DskipTests

# ==========================================
# Stage 3: Lightweight Production JRE Container
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=backend-builder /app/backend/target/job-recruitment-backend-1.0.0.jar app.jar

EXPOSE 8080 10000

ENTRYPOINT ["java", "-jar", "app.jar"]
