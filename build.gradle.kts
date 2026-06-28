plugins {
    kotlin("jvm") version "2.3.21"
    kotlin("plugin.serialization") version "2.4.0"
    id("com.gradleup.shadow") version "9.4.2"
}

group = "dev.boecker"
version = "0.1.0"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.boecker.dev/releases/")
    maven("https://repo.hypera.dev/snapshots/")
}

dependencies {
    implementation("net.minestom:minestom:2026.06.20-26.1.2")
    implementation("dev.hollowcube:polar:1.16.0")
    implementation("net.kyori:adventure-text-minimessage:5.1.1")

    implementation("dev.boecker.cherrycave.permissions:minestom:0.1.3")

    implementation("dev.boecker.cherrycave:common:0.1.0")

//    implementation("dev.kourier:amqp-client-robust:0.4.6")

    implementation("ch.qos.logback:logback-classic:1.5.34")
    implementation("io.github.oshai:kotlin-logging-jvm:8.0.4")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

kotlin {
    jvmToolchain(25)
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "dev.boecker.cherrycave.marathon.LauncherKt"
        }
    }

    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        mergeServiceFiles()
        archiveClassifier.set("")
    }
}