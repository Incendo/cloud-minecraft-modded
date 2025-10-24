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
    compileOnly("org.spongepowered:spongeapi:11.0.0-SNAPSHOT")
    compileOnly("org.spongepowered:sponge:1.20.6-11.0.0-SNAPSHOT")
}

neoForge {
    enable {
        neoFormVersion = "1.20.6-20240627.102356"
    }
}
