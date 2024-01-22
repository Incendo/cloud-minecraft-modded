import org.spongepowered.gradle.vanilla.repository.MinecraftPlatform

plugins {
    id("conventions.base")
    id("conventions.publishing")
    id("org.spongepowered.gradle.vanilla")
}

dependencies {
    api(libs.cloud.core)
    implementation(libs.cloud.brigadier)
    implementation(project(":cloud-minecraft-modded-common", configuration = "namedElements"))
    compileOnly("org.spongepowered:spongeapi:11.0.0-SNAPSHOT")
    compileOnly("org.spongepowered:sponge:1.20.2-11.0.0-SNAPSHOT")
}

minecraft {
    version("1.20.2")
    platform(MinecraftPlatform.JOINED)
}
