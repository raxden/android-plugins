package com.raxdenstudios

import com.raxdenstudios.publishing.extension.PublishExtension
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class ExtensionsTest {

    @TempDir
    lateinit var tempDir: Path

    private fun newProject(): Project = ProjectBuilder.builder()
        .withProjectDir(tempDir.toFile())
        .withGradleUserHomeDir(tempDir.resolve("gradle-home").toFile())
        .build()

    @Test
    fun `Given a plugin that is not applied When hasPlugin is asked Then it is false`() {
        val project = newProject()

        assertFalse(project.hasPlugin("java-platform"))
    }

    @Test
    fun `Given an applied plugin When hasPlugin is asked Then it is true`() {
        val project = newProject()
        project.pluginManager.apply("java-platform")

        assertTrue(project.hasPlugin("java-platform"))
    }

    @Test
    fun `Given a plugin applied after the callback When withPlugin is used Then the block runs once applied`() {
        val project = newProject()
        var calls = 0
        project.withPlugin("java-platform") { calls++ }
        assertEquals(0, calls)

        project.pluginManager.apply("java-platform")

        assertEquals(1, calls)
    }

    @Test
    fun `Given a plugin already applied When withPlugin is used Then the block runs immediately`() {
        val project = newProject()
        project.pluginManager.apply("java-platform")
        var calls = 0

        project.withPlugin("java-platform") { calls++ }

        assertEquals(1, calls)
    }

    @Test
    fun `Given a missing plugin When checkPlugin runs with the default message Then it names both plugins and the project`() {
        val project = newProject()

        val failure = assertThrows<GradleException> {
            project.checkPlugin(plugin = "java-platform", requiredBy = "my.plugin")
        }

        assertContains(failure.message.orEmpty(), "Plugin 'my.plugin' requires plugin 'java-platform'")
        assertContains(failure.message.orEmpty(), "project ':'")
    }

    @Test
    fun `Given a missing plugin When checkPlugin runs with a custom message Then it is used`() {
        val project = newProject()

        val failure = assertThrows<GradleException> {
            project.checkPlugin(plugin = "java-platform", requiredBy = "my.plugin") { "custom message" }
        }

        assertEquals("custom message", failure.message)
    }

    @Test
    fun `Given an applied plugin When checkPlugin runs Then it does not fail`() {
        val project = newProject()
        project.pluginManager.apply("java-platform")

        project.checkPlugin(plugin = "java-platform", requiredBy = "my.plugin")
    }

    @Test
    fun `Given a missing property and an existing environment variable When resolving Then the environment value is used`() {
        val project = newProject()
        val (name, value) = System.getenv().entries.first { it.value.isNotBlank() }

        assertEquals(value, project.gradlePropertyOrEnvironment(name).orNull)
    }

    @Test
    fun `Given neither a property nor an environment variable When resolving Then the provider is empty`() {
        val project = newProject()

        assertNull(project.gradlePropertyOrEnvironment("RAXDEN_SURELY_UNDEFINED_KEY").orNull)
    }

    @Test
    fun `Given no extension with that name When getOrCreateExtension is called Then it creates one of the type`() {
        val project = newProject()

        val extension = project.getOrCreateExtension<PublishExtension>("publishThing")

        assertSame(extension, project.extensions.getByName("publishThing"))
        assertSame(project, extension.project)
    }

    @Test
    fun `Given an extension of the type When getOrCreateExtension is called Then it returns the existing one`() {
        val project = newProject()
        val first = project.getOrCreateExtension<PublishExtension>("publishThing")

        val second = project.getOrCreateExtension<PublishExtension>("publishThing")

        assertSame(first, second)
    }

    @Test
    fun `Given an extension of another type When getOrCreateExtension is called Then it fails naming both types`() {
        val project = newProject()
        project.extensions.add("publishThing", "not a publish extension")

        val failure = assertThrows<IllegalStateException> {
            project.getOrCreateExtension<PublishExtension>("publishThing")
        }

        assertContains(failure.message.orEmpty(), "Extension 'publishThing' already exists with type java.lang.String")
        assertContains(failure.message.orEmpty(), PublishExtension::class.java.name)
    }
}
