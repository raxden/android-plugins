package com.raxdenstudios.publishing.configurer

import com.raxdenstudios.publishing.model.Developer
import com.raxdenstudios.publishing.model.License
import com.raxdenstudios.publishing.model.Pom
import com.raxdenstudios.publishing.model.Scm
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.internal.publication.MavenPomInternal
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class PomDataConfigurerTest {

    @TempDir
    lateinit var tempDir: Path

    private val fixture by lazy { ConfigurerTestProject(tempDir, name = "commons") }

    private val configurer by lazy { PomDataConfigurer(fixture.project, ConfigurerTestProject.EXTENSION_NAME) }

    private val validDeveloper = Developer(id = "raxden", name = "Dev", email = "dev@example.com")

    private fun validExtension(url: String = "https://github.com/raxden/commons") {
        fixture.extension.url.set(url)
        fixture.extension.developer.set(validDeveloper)
    }

    private fun configured(): MavenPomInternal {
        val publication: MavenPublication = fixture.publication()
        configurer.configure(publication)
        return publication.pom as MavenPomInternal
    }

    @Test
    fun `Given a valid extension When asking for problems Then there are none`() {
        validExtension()

        assertEquals(emptyList(), configurer.problems())
    }

    @Test
    fun `Given a blank url When asking for problems Then it asks for a url`() {
        fixture.extension.developer.set(validDeveloper)

        val problems = configurer.problems()

        assertEquals(1, problems.size)
        assertContains(problems.single(), "'url' is required")
    }

    @Test
    fun `Given a non http url When asking for problems Then it asks for an http url`() {
        validExtension(url = "git@github.com:raxden/commons.git")

        val problems = configurer.problems()

        assertEquals(1, problems.size)
        assertContains(problems.single(), "'url' must be an http(s) url, but was 'git@github.com:raxden/commons.git'")
    }

    @Test
    fun `Given no developer name When asking for problems Then it asks for a developer`() {
        fixture.extension.url.set("https://github.com/raxden/commons")

        val problems = configurer.problems()

        assertEquals(1, problems.size)
        assertContains(problems.single(), "at least one developer with a name is required")
    }

    @Test
    fun `Given no named license When asking for problems Then it asks for a license`() {
        validExtension()
        fixture.extension.pom.set(Pom.default.copy(licenses = listOf(License.default)))

        val problems = configurer.problems()

        assertEquals(1, problems.size)
        assertContains(problems.single(), "at least one license with a name is required")
    }

    @Test
    fun `Given everything missing When asking for problems Then all three are listed`() {
        fixture.extension.pom.set(Pom.default.copy(licenses = emptyList()))

        assertEquals(3, configurer.problems().size)
    }

    @Test
    fun `Given extension values When configuring Then the pom uses them`() {
        validExtension()
        fixture.extension.name.set("Commons")
        fixture.extension.description.set("Commons description")

        val pom = configured()

        assertEquals("Commons", pom.name.get())
        assertEquals("Commons description", pom.description.get())
        assertEquals("https://github.com/raxden/commons", pom.url.get())
    }

    @Test
    fun `Given pom values and extension values When configuring Then the pom values win`() {
        validExtension()
        fixture.extension.name.set("Extension name")
        fixture.extension.description.set("Extension description")
        fixture.extension.pom.set(
            Pom.default.copy(
                name = "Pom name",
                description = "Pom description",
                url = "https://example.com/from-pom",
            )
        )

        val pom = configured()

        assertEquals("Pom name", pom.name.get())
        assertEquals("Pom description", pom.description.get())
        assertEquals("https://example.com/from-pom", pom.url.get())
    }

    @Test
    fun `Given blank name and description When configuring Then project name and a generic text are used`() {
        validExtension()
        fixture.project.group = "com.project"

        val pom = configured()

        assertEquals("commons", pom.name.get())
        assertEquals("Publication for com.project:commons", pom.description.get())
    }

    @Test
    fun `Given a project description When configuring Then it is used as pom description`() {
        validExtension()
        fixture.project.description = "Project description"

        assertEquals("Project description", configured().description.get())
    }

    @Test
    fun `Given a blank url When configuring Then the pom url and scm are left unset`() {
        fixture.extension.developer.set(validDeveloper)

        val pom = configured()

        assertFalse(pom.url.isPresent)
        assertNull(pom.scm)
    }

    @Test
    fun `Given licenses with and without name When configuring Then only named licenses are written`() {
        validExtension()
        fixture.extension.pom.set(
            Pom.default.copy(
                licenses = listOf(
                    License(name = "MIT", url = "https://opensource.org/licenses/MIT"),
                    License(name = "No url", url = ""),
                    License.default,
                )
            )
        )

        val licenses = configured().licenses

        assertEquals(listOf("MIT", "No url"), licenses.map { it.name.get() })
        assertEquals("https://opensource.org/licenses/MIT", licenses.first().url.get())
        assertFalse(licenses.last().url.isPresent)
    }

    @Test
    fun `Given pom developers When configuring Then they replace the extension developer`() {
        validExtension()
        val other = Developer(id = "", name = "Other", email = "")
        fixture.extension.pom.set(Pom.default.copy(developers = listOf(other, Developer.default)))

        val developers = configured().developers

        assertEquals(listOf("Other"), developers.map { it.name.get() })
        assertFalse(developers.single().id.isPresent)
        assertFalse(developers.single().email.isPresent)
    }

    @Test
    fun `Given no pom developers When configuring Then the extension developer is written`() {
        validExtension()

        val developer = configured().developers.single()

        assertEquals("raxden", developer.id.get())
        assertEquals("Dev", developer.name.get())
        assertEquals("dev@example.com", developer.email.get())
    }

    @Test
    fun `Given a web url When configuring Then the scm is derived from it`() {
        validExtension()

        val scm = configured().scm!!

        assertEquals("scm:git:https://github.com/raxden/commons.git", scm.connection.get())
        assertEquals("scm:git:ssh://git@github.com/raxden/commons.git", scm.developerConnection.get())
        assertEquals("https://github.com/raxden/commons", scm.url.get())
    }

    @Test
    fun `Given explicit scm values When configuring Then they win over the derived ones`() {
        validExtension()
        fixture.extension.pom.set(
            Pom.default.copy(scm = Scm(connection = "scm:git:custom", developerConnection = "", url = ""))
        )

        val scm = configured().scm!!

        assertEquals("scm:git:custom", scm.connection.get())
        assertEquals("scm:git:ssh://git@github.com/raxden/commons.git", scm.developerConnection.get())
    }

    @Test
    fun `Given a non web url and no scm When configuring Then no scm block is written`() {
        validExtension(url = "git@github.com:raxden/commons.git")

        assertNull(configured().scm)
    }

    @Test
    fun `Given a non web url and an explicit scm When configuring Then only the explicit values are written`() {
        validExtension(url = "git@github.com:raxden/commons.git")
        fixture.extension.pom.set(Pom.default.copy(scm = Scm("scm:git:custom", "", "")))

        val scm = configured().scm!!

        assertEquals("scm:git:custom", scm.connection.get())
        assertFalse(scm.developerConnection.isPresent)
        assertTrue(!scm.url.isPresent)
    }

    @Test
    fun `Given all scm values explicit When configuring Then none is derived`() {
        validExtension()
        fixture.extension.pom.set(
            Pom.default.copy(scm = Scm("scm:git:custom", "scm:git:ssh://custom", "https://custom.example.com"))
        )

        val scm = configured().scm!!

        assertEquals("scm:git:custom", scm.connection.get())
        assertEquals("scm:git:ssh://custom", scm.developerConnection.get())
        assertEquals("https://custom.example.com", scm.url.get())
    }
}
