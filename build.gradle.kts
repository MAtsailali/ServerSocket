plugins {
    kotlin("jvm") version "1.9.0"
    id("com.github.johnrengelman.shadow") version "8.1.1" // Per crear el JAR executable
}

repositories {
    mavenCentral()
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest {
        attributes["Main-Class"] = "MainKt" // Nom del fitxer Main.kt -> MainKt
    }
}