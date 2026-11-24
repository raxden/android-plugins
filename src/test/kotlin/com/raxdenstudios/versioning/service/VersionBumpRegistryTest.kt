package com.raxdenstudios.versioning.service

import com.raxdenstudios.versioning.version.VersionPart
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

internal class VersionBumpRegistryTest {

    @TempDir
    lateinit var tempDir: Path

    private val project by lazy {
        ProjectBuilder.builder()
            .withProjectDir(tempDir.toFile())
            .withGradleUserHomeDir(tempDir.resolve("gradle-home").toFile())
            .build()
    }

    private val registry by lazy { VersionBumpRegistry.register(project).get() }

    private fun versionFile(name: String = "version.properties"): File = tempDir.resolve(name).toFile()

    @Test
    fun `Given a free file and part When claiming Then it returns null`() {
        assertNull(registry.claim(versionFile(), VersionPart.PATCH, ":a:increasePatchVersion"))
    }

    @Test
    fun `Given a claimed file and part When claiming again Then it returns the task that claimed it`() {
        registry.claim(versionFile(), VersionPart.PATCH, ":a:increasePatchVersion")

        val owner = registry.claim(versionFile(), VersionPart.PATCH, ":b:increasePatchVersion")

        assertEquals(":a:increasePatchVersion", owner)
    }

    @Test
    fun `Given a claimed part When claiming another part of the same file Then it is free`() {
        registry.claim(versionFile(), VersionPart.MINOR, ":a:increaseMinorVersion")

        assertNull(registry.claim(versionFile(), VersionPart.PATCH, ":a:increasePatchVersion"))
    }

    @Test
    fun `Given a claimed part When claiming it on another file Then it is free`() {
        registry.claim(versionFile("a.properties"), VersionPart.PATCH, ":a:increasePatchVersion")

        assertNull(registry.claim(versionFile("b.properties"), VersionPart.PATCH, ":b:increasePatchVersion"))
    }

    @Test
    fun `Given two paths to the same file When claiming through both Then the canonical path makes them collide`() {
        val direct = versionFile()
        val indirect = File(tempDir.toFile(), "nested/../version.properties")
        registry.claim(direct, VersionPart.PATCH, ":a:increasePatchVersion")

        val owner = registry.claim(indirect, VersionPart.PATCH, ":b:increasePatchVersion")

        assertEquals(":a:increasePatchVersion", owner)
    }

    @Test
    fun `Given a claim When its owner releases it Then another task can claim it`() {
        registry.claim(versionFile(), VersionPart.PATCH, ":a:increasePatchVersion")

        registry.release(versionFile(), VersionPart.PATCH, ":a:increasePatchVersion")

        assertNull(registry.claim(versionFile(), VersionPart.PATCH, ":b:increasePatchVersion"))
    }

    @Test
    fun `Given a claim When another task releases it Then the claim is kept`() {
        registry.claim(versionFile(), VersionPart.PATCH, ":a:increasePatchVersion")

        registry.release(versionFile(), VersionPart.PATCH, ":b:increasePatchVersion")

        assertEquals(":a:increasePatchVersion", registry.claim(versionFile(), VersionPart.PATCH, ":c:increasePatchVersion"))
    }

    @Test
    fun `Given a registered service When registering again Then the same instance is returned`() {
        assertSame(VersionBumpRegistry.register(project).get(), VersionBumpRegistry.register(project).get())
    }
}
