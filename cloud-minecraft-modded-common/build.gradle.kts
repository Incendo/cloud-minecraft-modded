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
    compileOnly(libs.adventureMod)

    compileOnly(libs.cloud.minecraft.extras)
}

tasks.withType(AbstractRemapJarTask::class).configureEach {
    targetNamespace = "named"
}

tasks.jar {
    manifest.attributes(
        "FMLModType" to "GAMELIBRARY",
        "Automatic-Module-Name" to "org.incendo.cloud.minecraft.modded.common",
    )
}
