# --------
# FRONTEND Docker Setup 
# --------
FROM node:22-alpine AS frontend-build
WORKDIR /app

# Copying package.json, install, then copy rest of source files before building
COPY ./Frontend/package.json ./
RUN npm install
COPY frontend_setup/ ./
RUN npm run build

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
RUN mvn clean package

# Copy the .jar file 
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar

# Expose website to port 8080
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
