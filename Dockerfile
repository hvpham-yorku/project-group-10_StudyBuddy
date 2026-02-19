# --------
# FRONTEND Docker Setup 
# --------
FROM node:22-alpine AS frontend-build
WORKDIR /app

# Copy frontend_setup files (signin/up branch frontend)
COPY ./Frontend/package.json ./Frontend/package-lock.json* ./
RUN npm install
COPY ./Frontend/ ./
RUN npx vite build

# --------
# BACKEND Docker Setup
# --------
FROM maven:3.9.6-eclipse-temurin-21 AS backend-build
WORKDIR /app

# Use built frontend files into static
COPY StudyBuddy/pom.xml .
COPY StudyBuddy/src ./src
COPY --from=frontend-build /app/dist ./src/main/resources/static

# Clean previous build attempts and create .jar file
RUN mvn clean package -DskipTests

# Copy the .jar file 
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar

# Expose website to port 8080
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
