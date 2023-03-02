import net.fabricmc.loom.task.AbstractRunTask

plugins {
    val indraVer = "3.0.1"
    id("net.kyori.indra") version indraVer
    id("net.kyori.indra.publishing") version indraVer
    id("net.kyori.indra.license-header") version indraVer
    id("dev.architectury.loom") version "1.1-SNAPSHOT"
}

indra {
    javaVersions().target(17)
}

license {
    header(file("HEADER"))
}

val transitiveInclude: Configuration by configurations.creating

loom.silentMojangMappingsLicense()

dependencies {
    minecraft("com.mojang:minecraft:1.19.3")
    mappings(loom.officialMojangMappings())
    forge("net.minecraftforge", "forge", "1.19.3-44.1.0")
    api(transitiveInclude(forgeExtra(platform("cloud.commandframework:cloud-bom:1.8.2"))!!)!!)
    api(transitiveInclude(forgeExtra("cloud.commandframework:cloud-core")!!)!!)
    api(transitiveInclude(forgeExtra("cloud.commandframework:cloud-brigadier")!!)!!)
}

java {
    withSourcesJar()
    withJavadocJar()
}

configurations.include {
    withDependencies {
        transitiveInclude.incoming.resolutionResult.allDependencies.map {
            (it as ResolvedDependencyResult).selected.id.displayName
        }.forEach { add(project.dependencies.create(it)) }
    }
}

tasks {
    withType<AbstractRunTask>().configureEach {
        systemProperty("cloud.test_commands", true)
    }
    processResources {
        inputs.property("version", project.version)
        filesMatching("META-INF/mods.toml") {
            expand("version" to project.version)
        }
    }
}
