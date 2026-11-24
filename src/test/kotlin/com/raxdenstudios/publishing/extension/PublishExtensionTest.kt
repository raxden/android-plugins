package com.raxdenstudios.publishing.extension

import com.raxdenstudios.publishing.model.Coordinates
import com.raxdenstudios.publishing.model.Developer
import com.raxdenstudios.publishing.model.Pom
import com.raxdenstudios.publishing.model.Repository
import com.raxdenstudios.publishing.model.Signing
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertEquals

internal class PublishExtensionTest {

    @TempDir
    lateinit var tempDir: Path

    private val project by lazy {
        ProjectBuilder.builder()
            .withProjectDir(tempDir.toFile())
            .withGradleUserHomeDir(tempDir.resolve("gradle-home").toFile())
            .build()
    }

    private fun newExtension(): PublishExtension =
        project.objects.newInstance(PublishExtension::class.java, project)

    @Test
    fun `Given a new extension When reading values Then every property has its default`() {
        val extension = newExtension()

        assertEquals("", extension.name.get())
        assertEquals("", extension.description.get())
        assertEquals("", extension.url.get())
        assertEquals(Developer.default, extension.developer.get())
        assertEquals(Coordinates.default, extension.coordinates.get())
        assertEquals(Pom.default, extension.pom.get())
        assertEquals(Repository.default, extension.repository.get())
        assertEquals(Signing.default, extension.signing.get())
    }

    @Test
    fun `Given a value assigned before reading When reading Then the assigned value wins over the default`() {
        val extension = newExtension()

        extension.name.set("My library")
        extension.coordinates.set(Coordinates.default.copy(artifactId = "my-lib"))

        assertEquals("My library", extension.name.get())
        assertEquals("my-lib", extension.coordinates.get().artifactId)
    }

    @Test
    fun `Given a property already read When assigning it again Then it fails because the value is final`() {
        val extension = newExtension()
        extension.url.get()

        val failure = assertThrows<IllegalStateException> { extension.url.set("https://example.com") }

        assertEquals(true, failure.message.orEmpty().contains("final"), failure.message)
    }

    @Test
    fun `Given a property already read When assigning a different property Then it still works`() {
        val extension = newExtension()
        extension.url.get()

        extension.name.set("Still assignable")

        assertEquals("Still assignable", extension.name.get())
    }
}
