import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    id("org.spongepowered.gradle.plugin") version "2.2.0"
    id("conventions.base")
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":cloud-sponge"))
    implementation(libs.cloud.minecraft.extras)
}

sponge {
    injectRepositories(false)
    apiVersion("11.0.0-SNAPSHOT")
    plugin("cloud-example-sponge") {
        loader {
            name(PluginLoaders.JAVA_PLAIN)
            version("1.0")
        }
        displayName("Cloud example Sponge plugin")
        description("Plugin to demonstrate and test the Sponge implementation of cloud")
        license("MIT")
        entrypoint("cloud.commandframework.examples.sponge.CloudExamplePlugin")
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
}

configurations {
    spongeRuntime {
        resolutionStrategy {
            cacheChangingModulesFor(1, "MINUTES")
            eachDependency {
                if (target.name == "spongevanilla") {
                    useVersion("1.20.+")
                }
            }
        }
    }
}

afterEvaluate {
    tasks.compileJava {
        // TODO - sponge AP not compatible with J21
        options.compilerArgs.remove("-Werror")
    }
}
