package com.raxdenstudios.publishing.extension

import com.raxdenstudios.publishing.PublishLibraryPlugin
import com.raxdenstudios.publishing.model.Coordinates
import com.raxdenstudios.publishing.model.Pom
import com.raxdenstudios.publishing.model.Repository
import com.raxdenstudios.publishing.model.Signing
import com.raxdenstudios.versioning.VersioningPlugin
import com.raxdenstudios.versioning.extension.AppVersioningExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import javax.inject.Inject

open class PublishLibraryExtension @Inject constructor(
    val project: Project
) {

    var name = ""
    var description = ""
    var url = ""
    var developerId = ""
    var developerName: String = ""
    var developerEmail: String = ""
    var coordinates: Coordinates = Coordinates.default
    var pom: Pom = Pom.default
    var repository: Repository = Repository.sonatype
    var signing: Signing = Signing.default

    companion object {

        fun create(project: Project): PublishLibraryExtension =
            project.extensions.create(PublishLibraryPlugin.EXTENSION_NAME)
    }
}
