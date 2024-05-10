plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "me.centauri07.chatreport"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven {
        url = uri("https://jitpack.io")
    }

    maven {
        url = uri("https://repo.mattstudios.me/artifactory/public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20-R0.1-SNAPSHOT")
    compileOnly("net.dv8tion:JDA:5.0.0-beta.23")
    compileOnly("com.charleskorn.kaml:kaml:0.54.0")

    implementation("dev.triumphteam:triumph-gui:3.1.7")
    implementation("com.github.andeng07:JarbAPI:a172928b4e")
}

kotlin {
    jvmToolchain(21)
}