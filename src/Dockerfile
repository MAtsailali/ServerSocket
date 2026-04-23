# Pas 1: Fem servir una imatge de Java (OpenJDK)
FROM openjdk:17-jdk-slim

# Pas 2: Creem el directori de treball
WORKDIR /app

# Pas 3: Copiem el fitxer JAR generat (després de fer ./gradlew shadowJar)
# El nom sol ser "projecte-all.jar"
COPY build/libs/*-all.jar app.jar

# Pas 4: Creem la carpeta uploads per evitar errors de permisos
RUN mkdir -p /app/uploads && chmod 777 /app/uploads

# Pas 5: Exposem el port del socket
EXPOSE 1234

# Pas 6: Comanda per executar el servidor
CMD ["java", "-jar", "app.jar"]