package com.raxdenstudios.publishing.configurer

import com.raxdenstudios.getOrCreateExtension
import com.raxdenstudios.publishing.extension.PublishExtension
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.testfixtures.ProjectBuilder
import java.nio.file.Path

/** In-process project with `maven-publish` and `signing` applied, for the configurer unit tests. */
internal class ConfigurerTestProject(tempDir: Path, name: String = "lib") {

    val project: Project = ProjectBuilder.builder()
        .withName(name)
        .withProjectDir(tempDir.resolve(name).toFile().apply { mkdirs() })
        .withGradleUserHomeDir(tempDir.resolve("gradle-home").toFile())
        .build()
        .also { project ->
            project.pluginManager.apply("maven-publish")
            project.pluginManager.apply("signing")
        }

    val extension: PublishExtension = project.getOrCreateExtension(EXTENSION_NAME)

    val publishing: PublishingExtension
        get() = project.extensions.getByType(PublishingExtension::class.java)

    fun publication(name: String = "platform"): MavenPublication =
        publishing.publications.create(name, MavenPublication::class.java)

    companion object {
        const val EXTENSION_NAME = "publishPlatform"
    }
}
