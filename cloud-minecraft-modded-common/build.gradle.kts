import net.fabricmc.loom.task.AbstractRemapJarTask

plugins {
    id("conventions.base")
    id("conventions.publishing")
    id("xyz.jpenilla.quiet-architectury-loom")
    id("conventions.common-dependencies")
}

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())
    compileOnly(libs.fabricLoader)
}

tasks.withType(AbstractRemapJarTask::class).configureEach {
    targetNamespace = "named"
}

tasks.jar {
    manifest.attributes("FMLModType" to "GAMELIBRARY")
}
