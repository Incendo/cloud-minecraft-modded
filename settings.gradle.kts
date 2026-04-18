pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://central.sonatype.com/repository/maven-snapshots/") {
            mavenContent { snapshotsOnly() }
        }
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.architectury.dev/")
        maven("https://repo.jpenilla.xyz/snapshots/")
    }
    includeBuild("gradle/build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("xyz.jpenilla.quiet-fabric-loom-repositories") version "1.16-SNAPSHOT"
    id("net.neoforged.moddev.repositories") version "2.0.141"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://central.sonatype.com/repository/maven-snapshots/") {
            mavenContent { snapshotsOnly() }
        }
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
    }

    versionCatalogs {
        create("fabricApiLibs") {
            from("net.fabricmc.fabric-api:fabric-api-catalog:0.144.3+26.1")
        }
    }
}

rootProject.name = "cloud-minecraft-modded"

include("cloud-minecraft-modded-common")
include("cloud-fabric")
include("cloud-neoforge")
