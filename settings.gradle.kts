pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.architectury.dev/")
        maven("https://repo.jpenilla.xyz/snapshots/")
    }
    includeBuild("gradle/build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    id("xyz.jpenilla.quiet-architectury-loom") version "1.4-SNAPSHOT"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // TODO temporary
        mavenLocal {
            mavenContent {
                includeGroup("cloud.commandframework")
                includeGroup("org.incendo")
                snapshotsOnly()
            }
        }

        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
    }
}

rootProject.name = "cloud-minecraft-modded"

include(":cloud-fabric")
include(":cloud-neoforge")
