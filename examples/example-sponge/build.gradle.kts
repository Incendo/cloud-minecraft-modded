import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    id("org.spongepowered.gradle.plugin") version "2.3.1-SNAPSHOT"
    id("conventions.base")
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":cloud-sponge"))
    implementation(libs.cloud.minecraft.extras)
}

sponge {
    injectRepositories(false)
    apiVersion("12.1.0-SNAPSHOT")
    minecraftVersion("1.21.1")
    plugin("cloud-example-sponge") {
        loader {
            name(PluginLoaders.JAVA_PLAIN)
            version("1.0")
        }
        displayName("Cloud example Sponge plugin")
        description("Plugin to demonstrate and test the Sponge implementation of cloud")
        license("MIT")
        entrypoint("org.incendo.cloud.examples.sponge.CloudExamplePlugin")
        dependency("spongeapi") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
    }
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }

    shadowJar {
        dependencies {
            exclude(dependency("io.leangen.geantyref:.*"))
        }
    }
}
