import me.modmuss50.mpp.ReleaseType
import me.modmuss50.mpp.platforms.modrinth.ModrinthEnvironment

plugins {
    id("conventions.publishing")
    id("me.modmuss50.mod-publish-plugin")
}

publishMods.modrinth {
    projectId = "dGpAFG2X"
    type = if (project.version.toString().contains("-beta")) ReleaseType.BETA else ReleaseType.STABLE
    environment = ModrinthEnvironment.CLIENT_OR_SERVER
    changelog = providers.environmentVariable("RELEASE_NOTES")
    accessToken = providers.environmentVariable("MODRINTH_TOKEN")
    minecraftVersions = providers.gradleProperty("modrinthMinecraftVersions").map {
        it.split(',').map(String::trim)
    }
}
