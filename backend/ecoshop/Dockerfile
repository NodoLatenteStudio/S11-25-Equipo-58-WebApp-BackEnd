# Etapa de construcción
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .

# Dar permisos de ejecución al Maven wrapper
RUN chmod +x mvnw

# Descargar dependencias (se cachean si no cambia el pom.xml)
RUN ./mvnw dependency:go-offline -B

# Copiar el código fuente
COPY src ./src

# Construir la aplicación
RUN ./mvnw clean package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear usuario no root para seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiar el JAR desde la etapa de construcción
COPY --from=build /app/target/ecoshop-0.0.1-SNAPSHOT.jar app.jar

# Exponer el puerto
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]

