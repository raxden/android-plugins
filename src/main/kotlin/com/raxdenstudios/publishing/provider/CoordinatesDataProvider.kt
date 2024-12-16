package com.raxdenstudios.publishing.provider

import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

class CoordinatesDataProvider(
    private val project: Project
) : DataProvider(project) {

    private val coordinates = extension.coordinates

    private val groupId: String
        get() = coordinates.groupId.ifEmpty { project.group.toString() }

    private val artifactId: String
        get() = coordinates.artifactId.ifEmpty { project.name }

    private val version: String
        get() = coordinates.version.ifEmpty { project.version.toString() }

    fun configure(publication: MavenPublication) {
        publication.run {
            groupId = this@CoordinatesDataProvider.groupId
            artifactId = this@CoordinatesDataProvider.artifactId
            version = this@CoordinatesDataProvider.version
        }
    }
}
