pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots/") {
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
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    id("xyz.jpenilla.quiet-architectury-loom") version "1.7-SNAPSHOT"
    id("org.spongepowered.gradle.vanilla") version "0.2.1-SNAPSHOT"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots/") {
            mavenContent { snapshotsOnly() }
        }
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://repo.spongepowered.org/repository/maven-public/") {
            mavenContent { includeGroup("org.spongepowered") }
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
