plugins {
    id("conventions.base")
    id("conventions.publishing")
    id("xyz.jpenilla.quiet-architectury-loom")
}

configurations {
    transitiveInclude {
        extendsFrom(api.get())

        exclude("org.checkerframework")
        exclude("org.apiguardian")
        exclude("org.incendo", "cloud-minecraft-modded-common")
    }
    forgeExtra {
        extendsFrom(api.get())
    }
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())
    neoForge(libs.neoForge)

    api(platform(libs.cloud.bom))
    api(libs.cloud.core)
    api(platform(libs.cloud.minecraft.bom))
    api(libs.cloud.brigadier)
    offlineLinkedJavadoc(project(":cloud-minecraft-modded-common"))
    api(project(":cloud-minecraft-modded-common", configuration = "namedElements"))
    include(project(":cloud-minecraft-modded-common"))
}

tasks {
    processResources {
        val props = mapOf("version" to project.version)
        inputs.properties(props)
        filesMatching("META-INF/neoforge.mods.toml") {
            expand(props)
        }
    }
}

loom {
    runs.configureEach {
        vmArg("-Dcloud.test_commands=true")
    }
}
