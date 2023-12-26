plugins {
    val indraVer = "3.1.3"
    id("net.kyori.indra") version indraVer
    id("net.kyori.indra.publishing") version indraVer
    id("net.kyori.indra.publishing.sonatype") version indraVer
    id("net.kyori.indra.licenser.spotless") version indraVer
    id("xyz.jpenilla.quiet-architectury-loom") version "1.4-SNAPSHOT"
}

indra {
    javaVersions {
        target(17)
    }
    github("Incendo", "cloud-neoforge") {
        ci(true)
    }
    mitLicense()
}

indraSpotlessLicenser {
    licenseHeaderFile(file("HEADER"))
}

configurations {
    transitiveInclude {
        extendsFrom(api.get())

        exclude("org.checkerframework")
        exclude("org.apiguardian")
    }
    forgeExtra {
        extendsFrom(api.get())
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.neoforged.net/releases/")
}

dependencies {
    minecraft("com.mojang:minecraft:1.20.4")
    mappings(loom.officialMojangMappings())
    neoForge("net.neoforged:neoforge:20.4.43-beta")

    api(platform("cloud.commandframework:cloud-bom:2.0.0-SNAPSHOT"))
    api("cloud.commandframework:cloud-core")
    api("cloud.commandframework:cloud-brigadier")
}

tasks {
    processResources {
        val props = mapOf("version" to project.version)
        inputs.properties(props)
        filesMatching("META-INF/mods.toml") {
            expand(props)
        }
    }
}

loom {
    runs.configureEach {
        vmArg("-Dcloud.test_commands=true")
    }
}
