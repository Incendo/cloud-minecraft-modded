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

    api(platform(libs.cloud.bom))
    api(libs.cloud.core)
    api(libs.cloud.brigadier)
}

tasks.withType(AbstractRemapJarTask::class).configureEach {
    targetNamespace = "named"
}
