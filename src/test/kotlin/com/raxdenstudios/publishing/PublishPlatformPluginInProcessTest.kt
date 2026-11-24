package com.raxdenstudios.publishing

import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertSame

internal class PublishPlatformPluginInProcessTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `Given a publication with the plugin's name When the plugin is applied Then it is kept and no second one is registered`() {
        val project = ProjectBuilder.builder()
            .withProjectDir(tempDir.toFile())
            .withGradleUserHomeDir(tempDir.resolve("gradle-home").toFile())
            .build()
        project.pluginManager.apply("java-platform")
        project.pluginManager.apply("maven-publish")
        val publishing = project.extensions.getByType(PublishingExtension::class.java)
        val existing = publishing.publications.create("platform", MavenPublication::class.java)

        project.pluginManager.apply(PublishPlatformPlugin::class.java)

        assertEquals(listOf("platform"), publishing.publications.names.toList())
        assertSame(existing, publishing.publications.getByName("platform"))
    }
}
