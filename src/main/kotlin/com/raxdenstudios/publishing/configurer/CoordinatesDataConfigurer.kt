package com.raxdenstudios.publishing.configurer

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

/**
 * Sets groupId, artifactId and version of the publication. Blank values in the extension's
 * coordinates fall back to the project group, name and version. A missing group or version fails
 * the configuration with a [GradleException].
 *
 * Must run once the build script has been evaluated, because [MavenPublication] coordinates are
 * plain (non-lazy) values.
 */
class CoordinatesDataConfigurer(
    private val project: Project,
    extensionName: String,
) : PublishingConfigurer<MavenPublication>(project, extensionName) {

    private val coordinates
        get() = extension.coordinates.get()

    private val groupId: String
        get() = coordinates.groupId
            .ifBlank { project.group.toString() }
            .ifBlank {
                throw GradleException("Publication groupId OR group is missing for project '${project.path}'.")
            }

    private val artifactId: String
        get() = coordinates.artifactId.ifBlank { project.name }

    private val version: String
        get() = coordinates.version
            .ifBlank { project.version.toString() }
            .ifBlank { throw GradleException("Publication version is missing for project '${project.path}'.") }

    override fun configure(data: MavenPublication) = data.run {
        groupId = this@CoordinatesDataConfigurer.groupId
        artifactId = this@CoordinatesDataConfigurer.artifactId
        version = this@CoordinatesDataConfigurer.version
    }
}
