import net.fabricmc.loom.task.AbstractRunTask

plugins {
    id("conventions.base")
    id("conventions.publishing")
    id("xyz.jpenilla.quiet-architectury-loom")
}

configurations {
    all {
        resolutionStrategy {
            force("net.fabricmc:fabric-loader:${libs.versions.fabricLoader.get()}")
        }
    }

    transitiveInclude {
        extendsFrom(api.get())

        exclude("org.checkerframework")
        exclude("org.apiguardian")
        exclude("cloud.commandframework", "cloud-minecraft-modded-common")
    }
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())
    modImplementation(libs.fabricLoader)

    api(platform(libs.cloud.bom))
    api(libs.cloud.core)
    api(libs.cloud.brigadier)
    api(project(":cloud-minecraft-modded-common", configuration = "namedElements"))
    include(project(":cloud-minecraft-modded-common"))

    modImplementation(platform(libs.fabricApi.bom))
    modImplementation(libs.fabricApi.command.api.v2)
    modImplementation(libs.fabricApi.networking.api.v1)
    modImplementation(libs.fabricApi.lifecycle.events.v1)

    modApi(libs.fabricPermissionsApi)
    include(libs.fabricPermissionsApi)
}

tasks {
    withType<ProcessResources>().configureEach() {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }

    withType<Javadoc>().configureEach {
        (options as? StandardJavadocDocletOptions)?.apply {
            // links("https://maven.fabricmc.net/docs/yarn-${Versions.fabricMc}+build.${Versions.fabricYarn}/") // todo
        }
    }

    withType<AbstractRunTask>().configureEach {
        standardInput = System.`in`
        jvmArgumentProviders += CommandLineArgumentProvider {
            if (System.getProperty("idea.active")?.toBoolean() == true || // IntelliJ
                System.getenv("TERM") != null || // linux terminals
                System.getenv("WT_SESSION") != null
            ) { // Windows terminal
                listOf("-Dfabric.log.disableAnsi=false")
            } else {
                listOf()
            }
        }
    }
}

/* set up a testmod source set */
val testmod: SourceSet by sourceSets.creating {
    val main = sourceSets.main.get()
    compileClasspath += main.compileClasspath
    runtimeClasspath += main.runtimeClasspath
    dependencies.add(implementationConfigurationName, main.output)
}

val testmodJar by tasks.registering(Jar::class) {
    archiveClassifier.set("testmod-dev")
    group = LifecycleBasePlugin.BUILD_GROUP
    from(testmod.output)
}

tasks.withType<AbstractRunTask> {
    classpath(testmodJar)
}
/* end of testmod setup */
