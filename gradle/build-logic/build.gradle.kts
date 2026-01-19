plugins {
    `kotlin-dsl`
    alias(libs.plugins.cloud.buildLogic.spotless)
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://central.sonatype.com/repository/maven-snapshots/") {
        mavenContent { snapshotsOnly() }
    }
}

dependencies {
    // loom needs this version of asm, for some reason we have an older one on the classpath without this
    implementation("org.ow2.asm:asm:9.9.1")
    implementation(libs.cloud.build.logic)

    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

cloudSpotless {
    licenseHeaderFile.convention(null as RegularFile?)
    ktlintVersion = libs.versions.ktlint
}
