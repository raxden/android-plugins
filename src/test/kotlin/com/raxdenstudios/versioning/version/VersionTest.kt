package com.raxdenstudios.versioning.version

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VersionTest {

    private val version = Version(major = 1, minor = 3, patch = 4, dev = 2)

    @Test
    fun `Given a version When versionCode is computed Then it combines every part`() {
        assertEquals(1_030_402, version.versionCode)
    }

    @Test
    fun `Given a version When versionName is computed Then it excludes dev`() {
        assertEquals("1.3.4", version.versionName)
    }

    @Test
    fun `Given a version When toString is called Then it prints name and code`() {
        assertEquals("1.3.4(1030402)", version.toString())
    }

    @Test
    fun `Given a version When major is increased Then lower parts are reset`() {
        assertEquals(Version(2, 0, 0, 0), version.increase(VersionPart.MAJOR))
    }

    @Test
    fun `Given a version When minor is increased Then patch and dev are reset`() {
        assertEquals(Version(1, 4, 0, 0), version.increase(VersionPart.MINOR))
    }

    @Test
    fun `Given a version When patch is increased Then dev is reset`() {
        assertEquals(Version(1, 3, 5, 0), version.increase(VersionPart.PATCH))
    }

    @Test
    fun `Given a version When dev is increased Then only dev changes`() {
        assertEquals(Version(1, 3, 4, 3), version.increase(VersionPart.DEV))
    }

    @Test
    fun `Given a valid version When validated Then there are no errors`() {
        assertTrue(version.validationErrors().isEmpty())
    }

    @Test
    fun `Given the maximum value of minor patch and dev When validated Then there are no errors`() {
        assertTrue(Version(1, 99, 99, 99).validationErrors().isEmpty())
    }

    @Test
    fun `Given minor 100 When validated Then minor is rejected`() {
        assertContains(Version(1, 100, 0, 0).validationErrors().single(), "MINOR")
    }

    @Test
    fun `Given patch 100 When validated Then patch is rejected`() {
        assertContains(Version(1, 0, 100, 0).validationErrors().single(), "PATCH")
    }

    @Test
    fun `Given dev 100 When validated Then dev is rejected`() {
        assertContains(Version(1, 0, 0, 100).validationErrors().single(), "DEV")
    }

    @Test
    fun `Given negative parts When validated Then each one is reported`() {
        val errors = Version(-1, -1, -1, -1).validationErrors()

        assertEquals(4, errors.size)
        listOf("MAJOR", "MINOR", "PATCH", "DEV").forEach { key ->
            assertTrue(errors.any { it.startsWith(key) }, "Missing error for $key in $errors")
        }
    }

    @Test
    fun `Given major 2100 When validated Then it is the highest accepted versionCode`() {
        assertTrue(Version(2100, 0, 0, 0).validationErrors().isEmpty())
    }

    @Test
    fun `Given major 2101 When validated Then versionCode over the Play limit is rejected`() {
        val errors = Version(2101, 0, 0, 0).validationErrors()

        assertContains(errors.single(), "exceeds Google Play's maximum of 2100000000")
    }
}
