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
    compileOnly("org.spongepowered:spongeapi:12.1.0-SNAPSHOT")
    compileOnly("org.spongepowered:sponge:1.21.1-12.0.3-SNAPSHOT")
}

neoForge {
    enable {
        neoFormVersion = "1.21.1-20240808.144430"
    }
}
