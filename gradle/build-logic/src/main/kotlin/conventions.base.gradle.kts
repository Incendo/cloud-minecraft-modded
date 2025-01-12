import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("org.incendo.cloud-build-logic")
    id("org.incendo.cloud-build-logic.spotless")
    id("org.incendo.cloud-build-logic.errorprone")
}

tasks {
    compileJava {
        options.errorprone {
            excludedPaths.set(".*[/\\\\]mixin[/\\\\].*")
        }
    }
}

indra {
    checkstyle().set(libs.versions.checkstyle)
    javaVersions {
        target(21)
        testWith().set(setOf(21))
        minimumToolchain(21)
    }
}

spotless {
    java {
        importOrderFile(rootProject.file(".spotless/cloud.importorder"))
    }
}

cloudSpotless {
    ktlintVersion.set(libs.versions.ktlint)
}

dependencies {
    checkstyle(libs.stylecheck)
    errorprone(libs.errorproneCore)
    compileOnly(libs.bundles.immutables)
    annotationProcessor(libs.bundles.immutables)
}

tasks {
    jar {
        manifest {
            attributes("Automatic-Module-Name" to "%s.%s".format(project.group, project.name.replace('-', '.')))
        }
    }
}
