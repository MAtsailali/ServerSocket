# ETAPA 1: Compilació
FROM gradle:8.3-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN ./gradlew shadowJar --no-daemon

# ETAPA 2: Execució
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
# Copiem el JAR generat des de l'etapa anterior
COPY --from=build /home/gradle/src/build/libs/*-all.jar app.jar
RUN mkdir -p /app/uploads && chmod 777 /app/uploads
EXPOSE 1234
CMD ["java", "-jar", "app.jar"]