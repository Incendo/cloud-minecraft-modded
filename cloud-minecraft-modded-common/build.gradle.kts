plugins {
    id("conventions.base")
    id("conventions.publishing")
    id("xyz.jpenilla.quiet-architectury-loom")
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())
    compileOnly(libs.fabricLoader)

    compileOnly(platform(libs.cloud.bom))
    compileOnly(libs.cloud.core)
    compileOnly(libs.cloud.brigadier)
}

tasks.jar {
    manifest {
        attributes("Fabric-Loom-Remap" to true)
    }
}
