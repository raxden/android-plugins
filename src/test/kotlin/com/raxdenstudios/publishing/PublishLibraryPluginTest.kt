package com.raxdenstudios.publishing

import org.gradle.testkit.runner.BuildResult
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class PublishLibraryPluginTest {

    @TempDir
    lateinit var projectDir: Path

    private val fixture by lazy { PublishingProjectFixture(projectDir) }

    private fun setUpDefaultProject() {
        fixture.writeSettings()
        fixture.writeLibrary()
        fixture.writePlatform()
    }

    private fun BuildResult.assertSucceededTask(path: String) {
        val outcome = task(path)?.outcome
        assertTrue(outcome?.name == "SUCCESS", "Expected $path to succeed but was $outcome\n$output")
    }

    private fun pomOf(module: File, publication: String): String =
        File(module, "build/publications/$publication/pom-default.xml").readText()

    @Test
    fun `Given a configured library When generating the release pom Then it contains the extension data`() {
        setUpDefaultProject()

        val result = fixture.runner(":lib:generatePomFileForReleasePublication").build()

        result.assertSucceededTask(":lib:generatePomFileForReleasePublication")
        val pom = pomOf(fixture.libDir, "release")
        assertContains(pom, "<groupId>com.example</groupId>")
        assertContains(pom, "<artifactId>commons-core</artifactId>")
        assertContains(pom, "<version>1.2.3</version>")
        assertContains(pom, "<name>Android</name>")
        assertContains(pom, "<description>Android Core</description>")
        assertContains(pom, "<url>https://github.com/raxden/android-commons</url>")
        assertContains(pom, "<id>raxden</id>")
        assertContains(pom, "<name>Dev</name>")
        assertContains(pom, "<email>dev@example.com</email>")
        assertContains(pom, "<name>The Apache License, Version 2.0</name>")
        assertContains(pom, "<connection>scm:git:https://github.com/raxden/android-commons.git</connection>")
        assertContains(
            pom,
            "<developerConnection>scm:git:ssh://git@github.com/raxden/android-commons.git</developerConnection>"
        )
    }

    @Test
    fun `Given blank coordinates When generating the pom Then project group name and version are used`() {
        fixture.writeSettings()
        fixture.writeLibrary(
            publishBlock = PublishingProjectFixture.DEFAULT_LIBRARY_PUBLISH
                .replace("""coordinates = Coordinates.default.copy(artifactId = "commons-core")""", "")
        )
        fixture.writePlatform()

        fixture.runner(":lib:generatePomFileForReleasePublication").build()

        val pom = pomOf(fixture.libDir, "release")
        assertContains(pom, "<groupId>com.example</groupId>")
        assertContains(pom, "<artifactId>lib</artifactId>")
        assertContains(pom, "<version>1.2.3</version>")
    }

    @Test
    fun `Given a configured platform When generating the platform pom Then it contains the extension data`() {
        setUpDefaultProject()

        val result = fixture.runner(":bom:generatePomFileForPlatformPublication").build()

        result.assertSucceededTask(":bom:generatePomFileForPlatformPublication")
        val pom = pomOf(fixture.bomDir, "platform")
        assertContains(pom, "<groupId>com.example</groupId>")
        assertContains(pom, "<artifactId>commons-bom</artifactId>")
        assertContains(pom, "<version>1.2.3</version>")
        assertContains(pom, "<name>BOM</name>")
        assertContains(pom, "<description>Commons BOM</description>")
        assertContains(pom, "<url>https://github.com/raxden/android-commons</url>")
        assertContains(pom, "<id>raxden</id>")
        assertContains(pom, "<name>The Apache License, Version 2.0</name>")
        assertContains(pom, "<connection>scm:git:https://github.com/raxden/android-commons.git</connection>")
    }

    @Test
    fun `Given the default repository When listing tasks Then no sonatype publish task exists`() {
        setUpDefaultProject()

        val result = fixture.runner(":lib:tasks", "--all").build()

        assertFalse(result.output.contains("ToSonatypeRepository"), result.output)
        assertFalse(result.output.contains("publishReleasePublicationToCustomRepository"), result.output)
    }

    @Test
    fun `Given a custom repository When listing tasks Then the publish task for it exists`() {
        fixture.writeSettings()
        fixture.writeLibrary(publishBlock = withCustomRepository())
        fixture.writePlatform()

        val result = fixture.runner(":lib:tasks", "--all").build()

        assertContains(result.output, "publishReleasePublicationToCustomRepository")
    }

    @Test
    fun `Given a custom repository and no signing keys When publishing to it Then it fails fast and nothing is published`() {
        fixture.writeSettings()
        fixture.writeLibrary(publishBlock = withCustomRepository())
        fixture.writePlatform()

        val result = fixture.runner(":lib:publishReleasePublicationToCustomRepository").buildAndFail()

        assertContains(result.output, "Signing is required")
        assertFalse(fixture.customRepoDir.exists() && fixture.customRepoDir.walkTopDown().any { it.isFile })
    }

    @Test
    fun `Given no signing keys When publishing to maven local Then it succeeds without signatures`() {
        setUpDefaultProject()

        val result = fixture.runner(
            ":bom:publishToMavenLocal",
            "-Dmaven.repo.local=${fixture.localMavenDir.absolutePath}",
        ).build()

        result.assertSucceededTask(":bom:publishToMavenLocal")
        val published = fixture.localMavenDir.walkTopDown().filter { it.isFile }.toList()
        assertTrue(published.any { it.name == "commons-bom-1.2.3.pom" }, published.toString())
        assertTrue(published.none { it.extension == "asc" }, published.toString())
    }

    @Test
    fun `Given the plugin applied before the android library plugin When generating the pom Then it works`() {
        fixture.writeSettings()
        fixture.writeLibrary(
            pluginsBlock = """
                id("com.raxdenstudios.android-publish-library")
                id("com.android.library")
            """.trimIndent()
        )
        fixture.writePlatform()

        val result = fixture.runner(":lib:generatePomFileForReleasePublication").build()

        result.assertSucceededTask(":lib:generatePomFileForReleasePublication")
        assertContains(pomOf(fixture.libDir, "release"), "<artifactId>commons-core</artifactId>")
    }

    @Test
    fun `Given publish library without the android library plugin When configuring Then it fails with a clear message`() {
        fixture.writeSettings()
        fixture.writeLibrary(
            pluginsBlock = """id("com.raxdenstudios.android-publish-library")""",
            publishBlock = PublishingProjectFixture.DEFAULT_LIBRARY_PUBLISH,
        )
        // The android {} block cannot be evaluated without the plugin: replace the whole script.
        File(fixture.libDir, "build.gradle.kts").writeText(
            """
            plugins { id("com.raxdenstudios.android-publish-library") }
            """.trimIndent()
        )
        fixture.writePlatform()

        val result = fixture.runner(":lib:tasks").buildAndFail()

        assertContains(result.output, "requires plugin 'com.android.library'")
    }

    @Test
    fun `Given no url When generating the pom Then it fails listing the problem`() {
        fixture.writeSettings()
        fixture.writeLibrary(
            publishBlock = PublishingProjectFixture.DEFAULT_LIBRARY_PUBLISH
                .replace("""url = "https://github.com/raxden/android-commons"""", "")
        )
        fixture.writePlatform()

        val result = fixture.runner(":lib:generatePomFileForReleasePublication").buildAndFail()

        assertContains(result.output, "Cannot publish project ':lib'")
        assertContains(result.output, "'url' is required")
    }

    @Test
    fun `Given no developer When generating the pom Then it fails listing the problem`() {
        fixture.writeSettings()
        fixture.writeLibrary(
            publishBlock = PublishingProjectFixture.DEFAULT_LIBRARY_PUBLISH
                .replace("""developer = Developer(id = "raxden", name = "Dev", email = "dev@example.com")""", "")
        )
        fixture.writePlatform()

        val result = fixture.runner(":lib:generatePomFileForReleasePublication").buildAndFail()

        assertContains(result.output, "at least one developer")
        assertFalse(result.output.contains("'url' is required"))
    }

    @Test
    fun `Given no url and no developer When generating the pom Then both problems are listed`() {
        fixture.writeSettings()
        fixture.writeLibrary(
            publishBlock = """
                group = "com.example"
                version = "1.0"
                publishLibrary { name = "Android" }
            """.trimIndent()
        )
        fixture.writePlatform()

        val result = fixture.runner(":lib:generatePomFileForReleasePublication").buildAndFail()

        assertContains(result.output, "'url' is required")
        assertContains(result.output, "at least one developer")
    }

    @Test
    fun `Given a non http url When generating the pom Then it fails asking for an http url`() {
        fixture.writeSettings()
        fixture.writeLibrary(
            publishBlock = PublishingProjectFixture.DEFAULT_LIBRARY_PUBLISH
                .replace("https://github.com/raxden/android-commons", "git@github.com:raxden/android-commons")
        )
        fixture.writePlatform()

        val result = fixture.runner(":lib:generatePomFileForReleasePublication").buildAndFail()

        assertContains(result.output, "must be an http(s) url")
    }

    @Test
    fun `Given no licenses When generating the pom Then it fails listing the license problem`() {
        fixture.writeSettings()
        fixture.writeLibrary(
            publishBlock = PublishingProjectFixture.DEFAULT_LIBRARY_PUBLISH +
                "\npublishLibrary { pom = Pom.default.copy(licenses = emptyList()) }"
        )
        fixture.writePlatform()

        val result = fixture.runner(":lib:generatePomFileForReleasePublication").buildAndFail()

        assertContains(result.output, "at least one license")
        assertFalse(result.output.contains("'url' is required"))
    }

    @Test
    fun `Given publish platform without java platform When configuring Then it fails with a clear message`() {
        fixture.writeSettings()
        fixture.writeLibrary()
        File(fixture.bomDir, "build.gradle.kts").also { it.parentFile.mkdirs() }.writeText(
            """plugins { id("com.raxdenstudios.android-publish-platform") }"""
        )

        val result = fixture.runner(":bom:tasks").buildAndFail()

        assertContains(result.output, "requires plugin 'java-platform'")
    }

    @Test
    fun `Given a repository with credentials When configuring Then that repository and credentials are used`() {
        fixture.writeSettings()
        fixture.writeLibrary(
            publishBlock = withCustomRepository(
                credentials = """Credentials(username = "u", password = "p")"""
            ),
            projectConfig = """
                tasks.register("printRepositories") {
                    doLast {
                        project.extensions.getByType(org.gradle.api.publish.PublishingExtension::class.java)
                            .repositories.filterIsInstance<MavenArtifactRepository>()
                            .forEach { println("REPO|${'$'}{it.name}|${'$'}{it.url}|${'$'}{it.credentials.username}|${'$'}{it.credentials.password}") }
                    }
                }
            """.trimIndent()
        )
        fixture.writePlatform()

        val result = fixture.runner(":lib:printRepositories", ":lib:tasks", "--all").build()

        assertContains(result.output, "REPO|custom|${fixture.customRepoDir.toURI()}|u|p")
        assertContains(result.output, "publishReleasePublicationToCustomRepository")
    }

    @Test
    fun `Given the configuration cache When publishing to maven local twice Then the second run reuses it`() {
        setUpDefaultProject()
        val args = arrayOf(
            ":bom:publishToMavenLocal",
            "-Dmaven.repo.local=${fixture.localMavenDir.absolutePath}",
        )

        val first = fixture.runnerWithConfigurationCache(*args).build()
        val second = fixture.runnerWithConfigurationCache(*args).build()

        assertContains(first.output, "Configuration cache entry stored")
        assertContains(second.output, "Reusing configuration cache")
        assertFalse(first.output.contains("problem"), first.output)
        assertFalse(second.output.contains("problem"), second.output)
    }

    @Test
    fun `Given the versioning plugin and no explicit version When generating the pom Then it uses the file version`() {
        fixture.writeSettings()
        fixture.writeLibrary(
            pluginsBlock = PublishingProjectFixture.LIBRARY_PLUGINS +
                "    id(\"com.raxdenstudios.android-versioning\")\n",
            publishBlock = PublishingProjectFixture.DEFAULT_LIBRARY_PUBLISH.replace("""version = "1.2.3"""", ""),
        )
        fixture.writePlatform()
        fixture.writeFile("lib/version.properties", "MAJOR=2\nMINOR=0\nPATCH=7\nDEV=0\n")

        fixture.runner(":lib:generatePomFileForReleasePublication").build()

        assertContains(pomOf(fixture.libDir, "release"), "<version>2.0.7</version>")
    }

    private fun withCustomRepository(
        credentials: String = "Credentials.default",
    ): String =
        PublishingProjectFixture.DEFAULT_LIBRARY_PUBLISH.replace(
            "publishLibrary {",
            """
            publishLibrary {
                repository = Repository(
                    name = "custom",
                    url = "${fixture.customRepoDir.toURI()}",
                    credentials = $credentials,
                )
            """.trimIndent()
        )
}
