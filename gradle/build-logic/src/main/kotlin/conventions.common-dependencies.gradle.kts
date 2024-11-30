plugins {
    id("conventions.base")
}

dependencies {
    api(platform(libs.cloud.bom))
    api(libs.cloud.core)
    api(platform(libs.cloud.minecraft.bom))
    api(libs.cloud.brigadier)
    compileOnly(libs.cloud.minecraft.signed.arguments)
    compileOnly(libs.adventureApi)
}
