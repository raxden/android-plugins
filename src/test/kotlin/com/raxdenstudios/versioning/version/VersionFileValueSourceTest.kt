package com.raxdenstudios.versioning.version

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class VersionFileValueSourceTest {

    @TempDir
    lateinit var tempDir: Path

    private val project by lazy {
        ProjectBuilder.builder()
            .withProjectDir(tempDir.toFile())
            .withGradleUserHomeDir(tempDir.resolve("gradle-home").toFile())
            .build()
    }

    private fun contents(name: String) = project.providers.of(VersionFileValueSource::class.java) {
        parameters.versionFile.set(project.layout.projectDirectory.file(name))
    }

    @Test
    fun `Given an existing version file When obtained Then its text is returned`() {
        tempDir.resolve("version.properties").toFile().writeText("MAJOR=1\n")

        assertEquals("MAJOR=1\n", contents("version.properties").get())
    }

    @Test
    fun `Given a missing version file When obtained Then it fails naming the file`() {
        val error = assertFailsWith<Exception> { contents("nope.properties").get() }

        assertContains(generateSequence<Throwable>(error) { it.cause }.joinToString { it.message.orEmpty() }, "nope.properties")
    }
}
