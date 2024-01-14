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
    api(libs.cloud.brigadier)
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
