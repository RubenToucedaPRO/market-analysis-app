# ETAPA 1: Construcción (Build)
# Usamos Maven sobre Alpine para compilar el proyecto
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# 1. Copiamos el pom.xml primero para aprovechar la caché de capas de Docker
COPY pom.xml ./

# 2. Descargamos las dependencias. Si el pom.xml no cambia, Docker saltará este paso
RUN mvn dependency:go-offline -B

# 3. Copiamos el código fuente y generamos el archivo .jar
COPY src ./src
RUN mvn clean package -DskipTests

# ETAPA 2: Ejecución (Runtime)
# Usamos una imagen mucho más pequeña que solo contiene el JRE (Java Runtime Environment)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 4. Seguridad: Creamos un usuario no-root para ejecutar la app
RUN addgroup -S spring && adduser -S spring -G spring

# 5. Copiamos asegurando el nombre (si usaste <finalName>app</finalName> en el pom)
COPY --from=build /app/target/*.jar app.jar

# 6. Cambiamos los permisos al usuario spring antes de cambiar de usuario
RUN chown spring:spring app.jar
USER spring

# 7. Exponemos el puerto interno (el que definimos a la derecha en el Compose)
EXPOSE 8080

# 8. Comando de ejecución optimizado para contenedores
ENTRYPOINT ["java", "-jar", "app.jar"]