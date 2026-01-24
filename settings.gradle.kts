pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://central.sonatype.com/repository/maven-snapshots/") {
            mavenContent { snapshotsOnly() }
        }
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.spongepowered.org/repository/maven-public/") {
            mavenContent { includeGroup("org.spongepowered") }
        }
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.architectury.dev/")
        maven("https://repo.jpenilla.xyz/snapshots/")
    }
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.spongepowered.org/repository/maven-public/")
    }
    includeBuild("gradle/build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("quiet-fabric-loom") version "1.15-SNAPSHOT"
    id("net.neoforged.moddev.repositories") version "2.0.140"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://central.sonatype.com/repository/maven-snapshots/") {
            mavenContent { snapshotsOnly() }
        }
        maven("https://repo.spongepowered.org/repository/maven-public/") {
            mavenContent { includeGroup("org.spongepowered") }
        }
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
    }

    versionCatalogs {
        create("fabricApiLibs") {
            from("net.fabricmc.fabric-api:fabric-api-catalog:0.141.1+1.21.11")
        }
    }
}

rootProject.name = "cloud-minecraft-modded"

include("cloud-minecraft-modded-common")
include("cloud-fabric")
include("cloud-fabric/common-repack")
findProject(":cloud-fabric/common-repack")?.name = "cloud-minecraft-modded-common-fabric-repack"
include("cloud-neoforge")
include("cloud-sponge")
include("examples/example-sponge")
findProject(":examples/example-sponge")?.name = "example-sponge"
