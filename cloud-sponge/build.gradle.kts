import org.spongepowered.gradle.vanilla.repository.MinecraftPlatform

plugins {
    id("conventions.base")
    id("org.spongepowered.gradle.vanilla")
}

java.disableAutoTargetJvm()

dependencies {
    api(libs.cloud.core)
    implementation(project(":cloud-brigadier"))
    compileOnly("org.spongepowered:spongeapi:11.0.0-SNAPSHOT")
    compileOnly("org.spongepowered:sponge:1.20.2-11.0.0-SNAPSHOT")
}

minecraft {
    version("1.20.2")
    platform(MinecraftPlatform.JOINED)
}
