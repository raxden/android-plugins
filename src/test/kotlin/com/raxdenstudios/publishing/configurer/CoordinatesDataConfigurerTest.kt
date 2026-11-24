package com.raxdenstudios.publishing.configurer

import com.raxdenstudios.publishing.model.Coordinates
import org.gradle.api.GradleException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertContains
import kotlin.test.assertEquals

internal class CoordinatesDataConfigurerTest {

    @TempDir
    lateinit var tempDir: Path

    private val fixture by lazy { ConfigurerTestProject(tempDir, name = "commons") }

    private fun configure(publication: org.gradle.api.publish.maven.MavenPublication = fixture.publication()) =
        CoordinatesDataConfigurer(fixture.project, ConfigurerTestProject.EXTENSION_NAME)
            .also { it.configure(publication) }
            .let { publication }

    @Test
    fun `Given explicit coordinates When configuring Then the publication uses them`() {
        fixture.project.group = "ignored.group"
        fixture.project.version = "0.0.1"
        fixture.extension.coordinates.set(Coordinates("com.example", "commons-core", "1.2.3"))

        val publication = configure()

        assertEquals("com.example", publication.groupId)
        assertEquals("commons-core", publication.artifactId)
        assertEquals("1.2.3", publication.version)
    }

    @Test
    fun `Given blank coordinates When configuring Then project group name and version are used`() {
        fixture.project.group = "com.project"
        fixture.project.version = "4.5.6"

        val publication = configure()

        assertEquals("com.project", publication.groupId)
        assertEquals("commons", publication.artifactId)
        assertEquals("4.5.6", publication.version)
    }

    @Test
    fun `Given a project version object When configuring Then its toString is used`() {
        fixture.project.group = "com.project"
        fixture.project.version = object {
            override fun toString() = "7.8.9"
        }

        assertEquals("7.8.9", configure().version)
    }

    @Test
    fun `Given neither coordinates group nor project group When configuring Then it fails naming the project path`() {
        fixture.project.version = "1.0.0"

        val failure = assertThrows<GradleException> { configure() }

        assertContains(failure.message.orEmpty(), "Publication groupId OR group is missing for project ':'")
    }

    @Test
    fun `Given neither coordinates version nor project version When configuring Then it fails naming the project path`() {
        fixture.project.group = "com.project"
        fixture.project.version = ""

        val failure = assertThrows<GradleException> { configure() }

        assertContains(failure.message.orEmpty(), "Publication version is missing for project ':'")
    }
}
