FROM eclipse-temurin:21-jdk
WORKDIR /app
# This copies the Java app you built into the container
COPY target/*.jar app.jar
# This copies your Firebase key so emails can send
COPY serviceAccountKey.json serviceAccountKey.json
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]