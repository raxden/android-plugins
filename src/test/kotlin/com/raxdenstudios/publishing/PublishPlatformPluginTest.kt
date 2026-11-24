package com.raxdenstudios.publishing

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** TestKit tests of the publish plugin on a `java-platform` module, which needs no Android SDK. */
internal class PublishPlatformPluginTest {

    @TempDir
    lateinit var projectDir: Path

    private val fixture by lazy { PublishingProjectFixture(projectDir) }

    private val key by lazy { PgpTestKey.generate() }

    private val publishTask = ":bom:publishPlatformPublicationToCustomRepository"

    private fun setUp(
        publishBlock: String = platformPublish(),
        projectConfig: String = "",
        extraPlugins: String = "",
    ) {
        fixture.writeSettings(includeLibrary = false)
        fixture.writePlatform(publishBlock = publishBlock, projectConfig = projectConfig, extraPlugins = extraPlugins)
    }

    /** Platform publish block with a file repository named `custom`, and [extra] lines inside `publishPlatform`. */
    private fun platformPublish(
        credentials: String = "Credentials.default",
        signing: String? = null,
        version: String? = "1.2.3",
    ): String = PublishingProjectFixture.DEFAULT_PLATFORM_PUBLISH
        .let { block -> if (version == null) block.replace("""version = "1.2.3"""", "") else block }
        .replace(
            "publishPlatform {",
            buildString {
                appendLine("publishPlatform {")
                appendLine(
                    """
                    repository = Repository(
                        name = "custom",
                        url = "${fixture.customRepoDir.toURI()}",
                        credentials = $credentials,
                    )
                    """.trimIndent()
                )
                if (signing != null) appendLine("signing = $signing")
            }
        )

    private fun publishedFiles(dir: File): List<String> =
        dir.walkTopDown().filter { it.isFile }.map { it.name }.toList()

    private fun keyFileReference(): String {
        fixture.writeFile("key.asc", key.armoredSecretKey)
        return """rootProject.file("key.asc").readText()"""
    }

    private fun printRepositoriesTask(): String = """
        tasks.register("printRepositories") {
            doLast {
                project.extensions.getByType(org.gradle.api.publish.PublishingExtension::class.java)
                    .repositories.filterIsInstance<MavenArtifactRepository>()
                    .forEach { println("REPO|${'$'}{it.name}|${'$'}{it.credentials.username}|${'$'}{it.credentials.password}") }
            }
        }
    """.trimIndent()

    @Test
    fun `Given signing properties without key id When publishing to a repository Then signature files are published`() {
        setUp()

        fixture.runner(
            publishTask,
            "-PSIGNING_KEY=${key.armoredSecretKey}",
            "-PSIGNING_PASSWORD=${key.password}",
        ).build()

        val files = publishedFiles(fixture.customRepoDir)
        assertContains(files, "commons-bom-1.2.3.pom")
        assertContains(files, "commons-bom-1.2.3.pom.asc")
        assertContains(files, "commons-bom-1.2.3.module.asc")
    }

    @Test
    fun `Given signing properties with key id When publishing to a repository Then signature files are published`() {
        setUp()

        fixture.runner(
            publishTask,
            "-PSIGNING_KEY_ID=${key.keyId}",
            "-PSIGNING_KEY=${key.armoredSecretKey}",
            "-PSIGNING_PASSWORD=${key.password}",
        ).build()

        assertContains(publishedFiles(fixture.customRepoDir), "commons-bom-1.2.3.pom.asc")
    }

    @Test
    fun `Given the signing dsl with key id When publishing to a repository Then signature files are published`() {
        val keyReference = keyFileReference()
        setUp(
            platformPublish(
                signing = """Signing(keyId = "${key.keyId}", key = $keyReference, password = "${key.password}")"""
            )
        )

        fixture.runner(publishTask).build()

        assertContains(publishedFiles(fixture.customRepoDir), "commons-bom-1.2.3.pom.asc")
    }

    @Test
    fun `Given the signing dsl without key id When publishing to a repository Then signature files are published`() {
        val keyReference = keyFileReference()
        setUp(platformPublish(signing = """Signing(keyId = "", key = $keyReference, password = "${key.password}")"""))

        fixture.runner(publishTask).build()

        assertContains(publishedFiles(fixture.customRepoDir), "commons-bom-1.2.3.pom.asc")
    }

    @Test
    fun `Given signing keys When publishing to maven local Then signature files are installed`() {
        setUp()

        fixture.runner(
            ":bom:publishToMavenLocal",
            "-Dmaven.repo.local=${fixture.localMavenDir.absolutePath}",
            "-PSIGNING_KEY=${key.armoredSecretKey}",
            "-PSIGNING_PASSWORD=${key.password}",
        ).build()

        val files = publishedFiles(fixture.localMavenDir)
        assertContains(files, "commons-bom-1.2.3.pom")
        assertContains(files, "commons-bom-1.2.3.pom.asc")
    }

    @Test
    fun `Given a wrong key password When publishing to a repository Then it fails and nothing is published`() {
        setUp()

        val result = fixture.runner(
            publishTask,
            "-PSIGNING_KEY=${key.armoredSecretKey}",
            "-PSIGNING_PASSWORD=wrong-password",
        ).buildAndFail()

        assertFalse(result.output.contains("BUILD SUCCESSFUL"), result.output)
        assertTrue(publishedFiles(fixture.customRepoDir).none { it.endsWith(".asc") })
    }

    @Test
    fun `Given blank dsl credentials and ossrh properties When configuring Then the properties are used`() {
        setUp(platformPublish(), projectConfig = printRepositoriesTask())

        val result = fixture.runner(
            ":bom:printRepositories",
            "-POSSRH_USERNAME=property-user",
            "-POSSRH_PASSWORD=property-password",
        ).build()

        assertContains(result.output, "REPO|custom|property-user|property-password")
    }

    @Test
    fun `Given dsl credentials and ossrh properties When configuring Then the dsl credentials win`() {
        setUp(
            platformPublish(credentials = """Credentials(username = "dsl-user", password = "dsl-password")"""),
            projectConfig = printRepositoriesTask(),
        )

        val result = fixture.runner(
            ":bom:printRepositories",
            "-POSSRH_USERNAME=property-user",
            "-POSSRH_PASSWORD=property-password",
        ).build()

        assertContains(result.output, "REPO|custom|dsl-user|dsl-password")
    }

    @Test
    fun `Given a dsl username and an ossrh password property When configuring Then each one is resolved separately`() {
        setUp(
            platformPublish(credentials = """Credentials(username = "dsl-user", password = "")"""),
            projectConfig = printRepositoriesTask(),
        )

        val result = fixture.runner(":bom:printRepositories", "-POSSRH_PASSWORD=property-password").build()

        assertContains(result.output, "REPO|custom|dsl-user|property-password")
    }

    @Test
    fun `Given blank dsl credentials and no fallback When configuring Then the repository has no credentials`() {
        setUp(platformPublish(), projectConfig = printRepositoriesTask())

        val result = fixture.runner(":bom:printRepositories").build()

        assertContains(result.output, "REPO|custom|null|null")
    }

    @Test
    fun `Given neither coordinates group nor project group When configuring Then it fails naming the project`() {
        // Gradle defaults a subproject's group to the root project name, so it has to be blanked.
        setUp(platformPublish().replace("""group = "com.example"""", "group = \"\""))

        val result = fixture.runner(":bom:generatePomFileForPlatformPublication").buildAndFail()

        assertContains(result.output, "Publication groupId OR group is missing for project ':bom'")
    }

    @Test
    fun `Given a blank project version and no coordinates version When configuring Then it fails naming the project`() {
        setUp(platformPublish().replace("""version = "1.2.3"""", "version = \"\""))

        val result = fixture.runner(":bom:generatePomFileForPlatformPublication").buildAndFail()

        assertContains(result.output, "Publication version is missing for project ':bom'")
    }

    @Test
    fun `Given an extension value assigned after the plugin read it When evaluating Then it fails because it is final`() {
        setUp(
            projectConfig = """afterEvaluate { publishPlatform.name = "too late" }""",
        )

        val result = fixture.runner(":bom:tasks").buildAndFail()

        assertContains(result.output, "final")
        assertContains(result.output, "property 'name'")
    }

    @Test
    fun `Given the versioning plugin and no explicit version When generating the pom Then it uses the file version`() {
        setUp(
            publishBlock = platformPublish(version = null),
            extraPlugins = """id("com.raxdenstudios.android-versioning")""",
        )
        fixture.writeFile("bom/version.properties", "MAJOR=3\nMINOR=1\nPATCH=4\nDEV=0\n")

        fixture.runner(":bom:generatePomFileForPlatformPublication").build()

        val pom = File(fixture.bomDir, "build/publications/platform/pom-default.xml").readText()
        assertContains(pom, "<version>3.1.4</version>")
    }

    @Test
    fun `Given a build script that fails after applying the plugin When evaluating Then only that failure is reported`() {
        setUp(
            publishBlock = "",
            projectConfig = """throw org.gradle.api.GradleException("script boom")""",
        )

        val result = fixture.runner(":bom:tasks").buildAndFail()

        assertContains(result.output, "script boom")
        assertFalse(result.output.contains("missing for project"))
    }
}
