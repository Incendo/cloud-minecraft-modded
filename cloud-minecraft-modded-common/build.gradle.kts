import net.fabricmc.loom.task.AbstractRemapJarTask

plugins {
    id("conventions.base")
    id("conventions.publishing")
    id("quiet-fabric-loom")
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
    manifest.attributes(
        "FMLModType" to "GAMELIBRARY",
        "Automatic-Module-Name" to "org.incendo.cloud_minecraft_modded_common",
    )
}
