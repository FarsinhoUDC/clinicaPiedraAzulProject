# ============================================================
# ETAPA 1: BUILD
# Usamos Maven con Java 17 para compilar el proyecto
# ============================================================
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copiamos primero solo el pom.xml para aprovechar el cache de Docker:
# si no cambiaron las dependencias, no las descarga de nuevo
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Ahora copiamos el código fuente y compilamos
# -DskipTests: omitimos tests en el build de producción
COPY src ./src
RUN mvn clean package -DskipTests -B

# ============================================================
# ETAPA 2: RUNTIME
# Imagen mucho más liviana, solo con el JRE para correr el JAR
# ============================================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiamos solo el JAR generado por la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Puerto que expone el contenedor (debe coincidir con server.port)
EXPOSE 8080

# Comando para arrancar Spring Boot con el perfil de producción
# Railway inyecta las variables de entorno automaticamente
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]