import net.fabricmc.loom.task.AbstractRemapJarTask

plugins {
    id("conventions.base")
    id("conventions.publishing")
    id("net.neoforged.moddev")
    id("conventions.common-dependencies")
}

neoForge {
    enable {
        neoFormVersion = libs.versions.neoform.get()
    }
}

dependencies {
    // todo: depend on common when deps fixed
    compileOnly(libs.adventureFabric)

    compileOnly(libs.cloud.minecraft.extras)
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
