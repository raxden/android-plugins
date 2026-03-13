package com.raxdenstudios.publishing.configurer

import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

class CoordinatesDataConfigurer(
    private val project: Project,
    extensionName: String,
) : PublishingConfigurer<MavenPublication>(project, extensionName) {

    private val groupId: String
        get() = extension.coordinates.groupId
            .ifBlank {
            project.group.toString().ifBlank {
                error("Publication groupId OR group is missing for project '${project.path}'.")
            }
        }

    private val artifactId: String
        get() = extension.coordinates.artifactId.ifBlank { project.name }

    private val version: String
        get() = extension.coordinates.version.ifBlank {
            project.version.toString().ifBlank {
                error("Publication version is missing for project '${project.path}'.")
            }
        }

    override fun configure(data: MavenPublication) = data.run {
        groupId = this@CoordinatesDataConfigurer.groupId
        artifactId = this@CoordinatesDataConfigurer.artifactId
        version = this@CoordinatesDataConfigurer.version
    }
}
