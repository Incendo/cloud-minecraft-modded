plugins {
    id("conventions.base")
    id("conventions.publishing")
    id("xyz.jpenilla.quiet-architectury-loom")
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())
    compileOnly(libs.fabricLoader)
}

tasks {
    val common = project(":cloud-minecraft-modded-common")
    jar {
        from(zipTree(common.tasks.jar.flatMap { it.archiveFile })) {
            exclude("META-INF/MANIFEST.MF")
        }
        manifest {
            attributes("Fabric-Loom-Remap" to true)
        }
    }
    sourcesJar {
        from(zipTree(common.tasks.sourcesJar.flatMap { it.archiveFile }))
    }
    javadocJar {
        from(zipTree(common.tasks.javadocJar.flatMap { it.archiveFile }))
    }
}
