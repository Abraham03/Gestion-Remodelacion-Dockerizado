# --- STAGE 1: Build de la aplicación Java con Maven ---
# Usamos una imagen que ya incluye Maven y el JDK, lo que simplifica el Dockerfile
FROM maven:3.9-eclipse-temurin-17-focal AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# --- STAGE 2: Creación de la imagen final ligera ---
# Usamos una imagen JRE (solo para ejecutar, no compilar) para reducir el tamaño
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Copiamos el JAR compilado desde la etapa 'build'
COPY --from=build /app/target/gestion-backend.jar app.jar

# Exponemos el puerto 8080 que usa la aplicación
EXPOSE 8080
# El ENTRYPOINT para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]