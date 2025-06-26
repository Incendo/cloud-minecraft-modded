import dev.lukebemish.centralportalpublishing.CentralPortalRepositoryHandlerExtension
import org.incendo.cloudbuildlogic.city
import org.incendo.cloudbuildlogic.jmp

plugins {
    id("org.incendo.cloud-build-logic.publishing")
    id("dev.lukebemish.central-portal-publishing")
}

indra {
    github("Incendo", "cloud-minecraft-modded") {
        ci(true)
    }
    mitLicense()

    configurePublications {
        pom {
            developers {
                jmp()
                city()
            }
        }
    }
}

javadocLinks {
    override(libs.fabricLoader, "https://maven.fabricmc.net/docs/fabric-loader-{version}")
    excludes.add("net.fabricmc.fabric-api:")
}

publishing {
    repositories {
        val portal = (this as ExtensionAware).extensions.getByType(CentralPortalRepositoryHandlerExtension::class)
        portal.portalBundle(":", "release")

        maven("https://central.sonatype.com/repository/maven-snapshots/") {
            name = "SonatypeSnapshots"
            credentials(PasswordCredentials::class) {
                username = providers.gradleProperty("sonatypeUsername").getOrElse("username")
                password = providers.gradleProperty("sonatypePassword").getOrElse("password")
            }
        }
    }
}
