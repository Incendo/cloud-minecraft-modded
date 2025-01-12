plugins {
    id("conventions.base")
    id("conventions.publishing")
    id("net.neoforged.moddev")
}

afterEvaluate {
    configurations.named("additionalRuntimeClasspath") {
        extendsFrom(configurations.api.get())

        exclude("org.incendo", "cloud-minecraft-modded-common")
    }
}

neoForge {
    enable {
        version = libs.versions.neoforge.get()
    }
    mods.register("cloud-neoforge") {
        sourceSet(sourceSets.main.get())
    }
    runs.register("client") {
        client()
    }
    runs.register("server") {
        server()
    }
    runs.configureEach {
        jvmArgument("-Dcloud.test_commands=true")
    }
}

dependencies {
    api(platform(libs.cloud.bom))
    api(libs.cloud.core)
    api(platform(libs.cloud.minecraft.bom))
    api(libs.cloud.brigadier)
    offlineLinkedJavadoc(project(":cloud-minecraft-modded-common"))
    api(project(":cloud-minecraft-modded-common", configuration = "namedElements"))

    jarJar(project(":cloud-minecraft-modded-common"))
    jarJar(libs.cloud.brigadier)
    jarJar(libs.cloud.core)
    jarJar(libs.cloud.services)
    jarJar(libs.geantyref)
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
