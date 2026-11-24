package com.raxdenstudios.publishing.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ScmTest {

    @Test
    fun `Given a github url When withUrl Then derives connections and keeps url`() {
        val scm = Scm.withUrl("https://github.com/o/r")

        assertEquals(
            Scm(
                connection = "scm:git:https://github.com/o/r.git",
                developerConnection = "scm:git:ssh://git@github.com/o/r.git",
                url = "https://github.com/o/r",
            ),
            scm,
        )
    }

    @Test
    fun `Given a url with trailing slash When withUrl Then slash is removed`() {
        val scm = Scm.withUrl("https://github.com/o/r/")

        assertEquals("scm:git:https://github.com/o/r.git", scm.connection)
        assertEquals("scm:git:ssh://git@github.com/o/r.git", scm.developerConnection)
        assertEquals("https://github.com/o/r", scm.url)
    }

    @Test
    fun `Given a url ending with dot git When withUrl Then suffix is not duplicated`() {
        val scm = Scm.withUrl("https://github.com/o/r.git")

        assertEquals("scm:git:https://github.com/o/r.git", scm.connection)
        assertEquals("scm:git:ssh://git@github.com/o/r.git", scm.developerConnection)
        assertEquals("https://github.com/o/r", scm.url)
    }

    @Test
    fun `Given a url with surrounding whitespace When withUrl Then it is trimmed`() {
        val scm = Scm.withUrl("  https://github.com/o/r  ")

        assertEquals("https://github.com/o/r", scm.url)
    }

    @Test
    fun `Given a branch When withUrl Then url points to the branch tree`() {
        val scm = Scm.withUrl("https://github.com/o/r", "main")

        assertEquals("https://github.com/o/r/tree/main", scm.url)
        assertEquals("scm:git:https://github.com/o/r.git", scm.connection)
    }

    @Test
    fun `Given a blank branch When withUrl Then url is the project url`() {
        val scm = Scm.withUrl("https://github.com/o/r", " ")

        assertEquals("https://github.com/o/r", scm.url)
    }

    @Test
    fun `Given an ssh style url When withUrl Then it throws IllegalArgumentException`() {
        assertFailsWith<IllegalArgumentException> { Scm.withUrl("git@github.com:o/r") }
        assertFailsWith<IllegalArgumentException> { Scm.withUrl("git@github.com:o/r", "main") }
    }

    @Test
    fun `Given http and https urls When isWebUrl Then it is true`() {
        assertTrue(Scm.isWebUrl("http://github.com/o/r"))
        assertTrue(Scm.isWebUrl("https://github.com/o/r"))
        assertTrue(Scm.isWebUrl("  https://github.com/o/r"))
    }

    @Test
    fun `Given a mixed case scheme When isWebUrl Then it is true`() {
        assertTrue(Scm.isWebUrl("HTTPS://github.com/o/r"))
        assertTrue(Scm.isWebUrl("HtTp://github.com/o/r"))
    }

    @Test
    fun `Given ssh style or other urls When isWebUrl Then it is false`() {
        assertFalse(Scm.isWebUrl("git@github.com:o/r"))
        assertFalse(Scm.isWebUrl("ssh://git@github.com/o/r"))
        assertFalse(Scm.isWebUrl("github.com/o/r"))
        assertFalse(Scm.isWebUrl(""))
    }
}
