package com.raxdenstudios.publishing.model

import kotlin.test.Test
import kotlin.test.assertEquals

internal class RepositoryTest {

    @Test
    @Suppress("DEPRECATION")
    fun `Given the deprecated sonatype repository When read Then it points to the shut down OSSRH staging url`() {
        assertEquals("sonatype", Repository.sonatype.name)
        assertEquals("https://oss.sonatype.org/service/local/staging/deploy/maven2/", Repository.sonatype.url)
    }
}
