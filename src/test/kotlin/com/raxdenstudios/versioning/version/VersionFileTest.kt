package com.raxdenstudios.versioning.version

import org.gradle.api.GradleException
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermissions
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VersionFileTest {

    @TempDir
    lateinit var tempDir: File

    @Test
    fun `Given a complete file When parsed Then every part is read in any order`() {
        val version = VersionFile.parse("DEV=2\nMAJOR=1\nPATCH=4\nMINOR=3\n", "v.properties")

        assertEquals(Version(1, 3, 4, 2), version)
    }

    @Test
    fun `Given missing keys When parsed Then they default to 0`() {
        assertEquals(Version(5, 0, 0, 0), VersionFile.parse("MAJOR=5", "v.properties"))
        assertEquals(Version(0, 0, 0, 0), VersionFile.parse("", "v.properties"))
    }

    @Test
    fun `Given a non integer value When parsed Then it fails mentioning key and path`() {
        val exception = assertFailsWith<GradleException> {
            VersionFile.parse("MINOR=abc", "some/v.properties")
        }

        assertContains(exception.message.orEmpty(), "some/v.properties")
        assertContains(exception.message.orEmpty(), "MINOR must be an integer")
    }

    @Test
    fun `Given an out of range value When parsed Then it fails mentioning the path`() {
        val exception = assertFailsWith<GradleException> {
            VersionFile.parse("PATCH=100", "some/v.properties")
        }

        assertContains(exception.message.orEmpty(), "some/v.properties")
        assertContains(exception.message.orEmpty(), "PATCH")
    }

    @Test
    fun `Given a missing file When read Then it fails mentioning the path`() {
        val file = File(tempDir, "missing.properties")

        val exception = assertFailsWith<GradleException> { VersionFile.read(file) }

        assertContains(exception.message.orEmpty(), file.path)
        assertContains(exception.message.orEmpty(), "does not exist")
    }

    @Test
    fun `Given a version When written Then lines are MAJOR MINOR PATCH DEV without comments`() {
        val file = File(tempDir, "v.properties").apply { writeText("DEV=9\nPATCH=9\n") }

        VersionFile.write(file, Version(1, 2, 3, 4))

        assertEquals(listOf("MAJOR=1", "MINOR=2", "PATCH=3", "DEV=4"), file.readLines())
        assertFalse(file.readText().contains("#"))
    }

    @Test
    fun `Given a file that does not exist When written Then it is created`() {
        val file = File(tempDir, "new.properties")

        VersionFile.write(file, Version(0, 1, 0, 0))

        assertEquals(listOf("MAJOR=0", "MINOR=1", "PATCH=0", "DEV=0"), file.readLines())
    }

    @Test
    fun `Given unknown keys When written Then they are preserved after the version keys`() {
        val file = File(tempDir, "v.properties").apply { writeText("ZED=z\nMAJOR=1\nALPHA=a\n") }

        VersionFile.write(file, Version(2, 0, 0, 0))

        assertEquals(
            listOf("MAJOR=2", "MINOR=0", "PATCH=0", "DEV=0", "ALPHA=a", "ZED=z"),
            file.readLines(),
        )
    }

    @Test
    fun `Given a written file When read Then the same version is returned`() {
        val file = File(tempDir, "v.properties")
        val version = Version(3, 14, 15, 92)

        VersionFile.write(file, version)

        assertEquals(version, VersionFile.read(file))
    }

    @Test
    fun `Given a write When it finishes Then no temp file is left behind`() {
        val file = File(tempDir, "v.properties")

        VersionFile.write(file, Version(1, 0, 0, 0))

        assertEquals(listOf("v.properties"), tempDir.list().orEmpty().toList())
    }

    @Test
    fun `Given extra values with special characters When written and read Then they survive`() {
        val file = File(tempDir, "v.properties")
        val extras = mapOf(
            "path" to "C:\\dir\\file",
            "url" to "http://host:80/a=b",
            "spaced key" to "  leading and trailing  ",
            "unicode" to "ñandú 日本語 \u00e9",
        )
        file.writeText(extras.entries.joinToString("\n", postfix = "\nMAJOR=1\n") { (k, v) -> escape(k, v) })

        VersionFile.write(file, Version(2, 0, 0, 0))

        val reloaded = java.util.Properties().apply { file.reader(Charsets.UTF_8).use { load(it) } }
        extras.forEach { (key, value) -> assertEquals(value, reloaded.getProperty(key), "key '$key'") }
        assertEquals(Version(2, 0, 0, 0), VersionFile.read(file))
        assertFalse(file.readText().contains("#"))
    }

    @Test
    fun `Given a file with restricted permissions When written Then the permissions are preserved`() {
        val file = File(tempDir, "v.properties").apply { writeText("MAJOR=1\n") }
        val path = file.toPath()
        Assumptions.assumeTrue(Files.getFileAttributeView(path, PosixFileAttributeView::class.java) != null)
        val permissions = PosixFilePermissions.fromString("rw-r-----")
        Files.setPosixFilePermissions(path, permissions)

        VersionFile.write(file, Version(1, 1, 0, 0))

        assertEquals(permissions, Files.getPosixFilePermissions(path))
    }

    @Test
    fun `Given a symlink When written through it Then the link is kept and the real file is updated`() {
        val real = File(tempDir, "real.properties").apply { writeText("MAJOR=1\n") }
        val link = File(tempDir, "link.properties")
        try {
            Files.createSymbolicLink(link.toPath(), real.toPath())
        } catch (_: UnsupportedOperationException) {
            Assumptions.abort<Unit>("Symlinks not supported")
        } catch (_: java.io.IOException) {
            Assumptions.abort<Unit>("Symlinks not permitted")
        }

        VersionFile.write(link, Version(1, 2, 0, 0))

        assertTrue(Files.isSymbolicLink(link.toPath()))
        assertEquals(Version(1, 2, 0, 0), VersionFile.read(real))
        assertEquals(real.readText(), link.readText())
    }

    private fun escape(key: String, value: String): String {
        val writer = java.io.StringWriter()
        java.util.Properties().apply { setProperty(key, value) }.store(writer, null)
        return writer.toString().lines().filterNot { it.startsWith("#") || it.isEmpty() }.joinToString("\n")
    }
}
