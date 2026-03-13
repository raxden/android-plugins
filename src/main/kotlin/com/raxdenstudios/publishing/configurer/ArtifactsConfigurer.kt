package com.raxdenstudios.publishing.configurer

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project

class ArtifactsConfigurer(
    project: Project,
    extensionName: String,
) : PublishingConfigurer<LibraryExtension>(project, extensionName) {

    companion object {
        private const val RELEASE = "release"
    }

    override fun configure(data: LibraryExtension) = data.run {
        publishing {
            singleVariant(RELEASE) {
                withSourcesJar()
            }
        }
    }
}
