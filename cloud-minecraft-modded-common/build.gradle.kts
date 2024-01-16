import net.fabricmc.loom.task.AbstractRemapJarTask

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

tasks.withType(AbstractRemapJarTask::class).configureEach {
    targetNamespace = "named"
}
