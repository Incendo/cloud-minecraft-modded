import net.fabricmc.loom.task.AbstractRunTask
import org.gradlex.javaecosystem.capabilities.rules.GuavaListenableFutureRule

plugins {
    val indraVer = "3.1.3"
    id("net.kyori.indra") version indraVer
    id("net.kyori.indra.publishing") version indraVer
    id("net.kyori.indra.publishing.sonatype") version indraVer
    id("net.kyori.indra.license-header") version indraVer
    id("dev.architectury.loom") version "1.4-SNAPSHOT"
}

repositories {
    maven("https://maven.neoforged.net/releases/")
}

indra {
    javaVersions().target(17)
}

license {
    header(file("HEADER"))
}

val transitiveInclude: Configuration by configurations.creating {
    exclude("org.checkerframework")
    exclude("org.apiguardian")
}

loom.silentMojangMappingsLicense()

dependencies {
    components.withModule(GuavaListenableFutureRule.MODULES[0]) {
        // Ad-hoc rule to revert the effect of 'GuavaListenableFutureRule' (NeoForge has broken dependencies)
        allVariants {
            withCapabilities {
                removeCapability(GuavaListenableFutureRule.CAPABILITY_GROUP, GuavaListenableFutureRule.CAPABILITY_NAME)
            }
        }
    }

    minecraft("com.mojang:minecraft:1.20.2")
    mappings(loom.officialMojangMappings())
    neoForge("net.neoforged", "neoforge", "20.2.86")

    listOf("api", transitiveInclude.name, "forgeExtra").forEach { c ->
        c(platform("cloud.commandframework:cloud-bom:1.8.4"))
        c("cloud.commandframework:cloud-core")
        c("cloud.commandframework:cloud-brigadier")
    }
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
