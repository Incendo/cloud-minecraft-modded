plugins {
    id("conventions.base")
    id("conventions.publishing")
    id("xyz.jpenilla.quiet-fabric-loom")
    id("conventions.common-dependencies")
}

dependencies {
    minecraft(libs.minecraft)
    compileOnly(libs.fabricLoader)
}

tasks {
    val common = project(":cloud-minecraft-modded-common")
    jar {
        from(zipTree(common.tasks.jar.flatMap { it.archiveFile })) {
            exclude("META-INF/MANIFEST.MF")
        }
    }
    sourcesJar {
        from(zipTree(common.tasks.sourcesJar.flatMap { it.archiveFile }))
    }
    javadocJar {
        from(zipTree(common.tasks.javadocJar.flatMap { it.archiveFile }))
    }
}
