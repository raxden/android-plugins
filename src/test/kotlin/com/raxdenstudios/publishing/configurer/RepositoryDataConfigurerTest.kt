package com.raxdenstudios.publishing.configurer

import com.raxdenstudios.publishing.model.Credentials
import com.raxdenstudios.publishing.model.Repository
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class RepositoryDataConfigurerTest {

    @TempDir
    lateinit var tempDir: Path

    private val fixture by lazy { ConfigurerTestProject(tempDir) }

    private val repositoryUrl get() = tempDir.resolve("repo").toUri().toString()

    private fun configure(): List<MavenArtifactRepository> {
        RepositoryDataConfigurer(fixture.project, ConfigurerTestProject.EXTENSION_NAME).configure(fixture.publishing)
        return fixture.publishing.repositories.filterIsInstance<MavenArtifactRepository>()
    }

    @Test
    fun `Given the default repository When configuring Then no repository is added`() {
        assertTrue(configure().isEmpty())
    }

    @Test
    fun `Given a repository with a url When configuring Then it is added with its name and url`() {
        fixture.extension.repository.set(Repository(name = "custom", url = repositoryUrl, credentials = Credentials.default))

        val repository = configure().single()

        assertEquals("custom", repository.name)
        assertEquals(tempDir.resolve("repo").toUri(), repository.url)
    }

    @Test
    fun `Given a repository without name When configuring Then Gradle's default name is kept`() {
        fixture.extension.repository.set(Repository(name = "", url = repositoryUrl, credentials = Credentials.default))

        assertEquals("maven", configure().single().name)
    }

    @Test
    fun `Given credentials in the extension When configuring Then they are set on the repository`() {
        fixture.extension.repository.set(
            Repository(name = "custom", url = repositoryUrl, credentials = Credentials("user", "secret"))
        )

        val credentials = configure().single().credentials

        assertEquals("user", credentials.username)
        assertEquals("secret", credentials.password)
    }

    @Test
    fun `Given only a password When configuring Then credentials are still set`() {
        fixture.extension.repository.set(
            Repository(name = "custom", url = repositoryUrl, credentials = Credentials("", "secret"))
        )

        val credentials = configure().single().credentials

        assertEquals("", credentials.username)
        assertEquals("secret", credentials.password)
    }

    @Test
    fun `Given blank credentials and no fallback When configuring Then the repository has no credentials`() {
        fixture.extension.repository.set(Repository(name = "custom", url = repositoryUrl, credentials = Credentials.default))

        val credentials = configure().single().credentials

        assertNull(credentials.username)
        assertNull(credentials.password)
    }
}
