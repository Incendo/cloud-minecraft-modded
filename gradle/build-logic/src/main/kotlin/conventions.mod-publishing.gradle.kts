import me.modmuss50.mpp.ReleaseType

plugins {
    id("conventions.publishing")
    id("me.modmuss50.mod-publish-plugin")
}

publishMods.modrinth {
    projectId = "dGpAFG2X"
    type = if (project.version.toString().contains("-beta")) ReleaseType.BETA else ReleaseType.STABLE
    changelog = providers.environmentVariable("RELEASE_NOTES")
    accessToken = providers.environmentVariable("MODRINTH_TOKEN")
    minecraftVersions = providers.gradleProperty("modrinthMinecraftVersions").map {
        it.split(',').map(String::trim)
    }
}
