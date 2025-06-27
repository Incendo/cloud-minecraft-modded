plugins {
    // alias(libs.plugins.cloud.buildLogic.rootProject.publishing)
    alias(libs.plugins.centralPublishing)
    alias(libs.plugins.cloud.buildLogic.rootProject.spotless)
}

spotlessPredeclare {
    kotlin { ktlint(libs.versions.ktlint.get()) }
    kotlinGradle { ktlint(libs.versions.ktlint.get()) }
}

tasks {
    spotlessCheck {
        dependsOn(gradle.includedBuild("build-logic").task(":spotlessCheck"))
    }
    spotlessApply {
        dependsOn(gradle.includedBuild("build-logic").task(":spotlessApply"))
    }
}

centralPortalPublishing.bundle("release") {
    username = providers.gradleProperty("sonatypeUsername")
    password = providers.gradleProperty("sonatypePassword")
    publishingType = "AUTOMATIC"
}
