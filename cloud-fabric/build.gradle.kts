import net.fabricmc.loom.task.AbstractRunTask

plugins {
    id("conventions.base")
    id("conventions.mod-publishing")
    id("xyz.jpenilla.quiet-fabric-loom")
}

publishMods.modrinth {
    file = tasks.jar.flatMap { it.archiveFile }
    modLoaders = listOf("fabric")
    requires("fabric-api")
    optional("fabric-permissions-api")
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
    }
}

dependencies {
    minecraft(libs.minecraft)
    implementation(libs.fabricLoader)

    api(platform(libs.cloud.bom))
    api(libs.cloud.core)
    api(platform(libs.cloud.minecraft.bom))
    api(libs.cloud.brigadier)

    compileOnly(libs.cloud.minecraft.signed.arguments)

    offlineLinkedJavadoc(project(":cloud-minecraft-modded-common"))
    api(project(":cloud-minecraft-modded-common"))
    include(project(":cloud-minecraft-modded-common"))

    implementation(platform(fabricApiLibs.bom))
    implementation(fabricApiLibs.command.api.v2)
    implementation(fabricApiLibs.networking.api.v1)
    implementation(fabricApiLibs.lifecycle.events.v1)

    compileOnly(libs.fabricPermissionsApi)
    localRuntime(libs.fabricPermissionsApi)
}

tasks {
    withType<ProcessResources>().configureEach {
        val props = mapOf("version" to project.version)
        inputs.properties(props)
        filesMatching("fabric.mod.json") {
            expand(props)
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

dependencies {
    localRuntime(libs.cloud.minecraft.signed.arguments)
    localRuntime(libs.adventureFabric)
    "testmodImplementation"(libs.adventureFabric)
    "testmodImplementation"(libs.cloud.minecraft.extras)
    localRuntime(libs.cloud.minecraft.extras)
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
