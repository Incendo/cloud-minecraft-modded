plugins {
    id("conventions.base")
    id("conventions.publishing")
    id("net.neoforged.moddev")
}

dependencies {
    api(libs.cloud.core)
    implementation(libs.cloud.brigadier)
    offlineLinkedJavadoc(project(":cloud-minecraft-modded-common"))
    implementation(project(":cloud-minecraft-modded-common"))
    compileOnly("org.spongepowered:spongeapi:19.0.0-SNAPSHOT")
    compileOnly("org.spongepowered:sponge:26.1.2-19.0.0-SNAPSHOT")
}

neoForge {
    enable {
        neoFormVersion = "26.1.2-1"
    }
}
