package com.raxdenstudios.publishing.configurer

import com.raxdenstudios.publishing.model.Developer
import com.raxdenstudios.publishing.model.License
import com.raxdenstudios.publishing.model.Scm
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.publish.maven.MavenPublication

/**
 * Fills the POM of the publication. Values in `PublishExtension.pom` win over the extension
 * properties, which win over the project data. The SCM block is derived from the project url (see
 * [Scm.withUrl]) unless it is set explicitly.
 *
 * POM licenses and developers are not lazy collections, so this must run once the build script has
 * been evaluated. [problems] lists what is missing for a valid Maven Central POM.
 */
class PomDataConfigurer(
    private val project: Project,
    private val extensionName: String,
) : PublishingConfigurer<MavenPublication>(project, extensionName) {

    private val pom
        get() = extension.pom.get()

    private val name: String
        get() = pom.name
            .ifBlank { extension.name.get() }
            .ifBlank { project.name }

    private val description: String
        get() = pom.description
            .ifBlank { extension.description.get() }
            .ifBlank { project.description.orEmpty() }
            .ifBlank { "Publication for ${project.group}:${project.name}" }

    private val url: String
        get() = pom.url.ifBlank { extension.url.get() }

    private val licenses: List<License>
        get() = pom.licenses.filter { license -> license.name.isNotBlank() }

    private val developers: List<Developer>
        get() = pom.developers
            .ifEmpty { listOf(extension.developer.get()) }
            .filter { developer -> developer.name.isNotBlank() }

    private val scm: Scm
        get() {
            val derived = if (Scm.isWebUrl(url)) Scm.withUrl(url) else Scm.default
            return Scm(
                connection = pom.scm.connection.ifBlank { derived.connection },
                developerConnection = pom.scm.developerConnection.ifBlank { derived.developerConnection },
                url = pom.scm.url.ifBlank { derived.url },
            )
        }

    /**
     * Returns one message per missing or invalid value, naming the extension setting to fix. Empty
     * when the POM is valid. Computed without touching the publication, so it can be called early.
     */
    fun problems(): List<String> = buildList {
        when {
            url.isBlank() -> add("'url' is required: set $extensionName { url = \"https://...\" }.")
            !Scm.isWebUrl(url) -> add("'url' must be an http(s) url, but was '$url'.")
        }
        if (developers.isEmpty()) {
            add("at least one developer with a name is required: set $extensionName { developer = Developer(...) }.")
        }
        if (licenses.isEmpty()) {
            add(
                "at least one license with a name is required: " +
                    "set $extensionName { pom = Pom.default.copy(licenses = listOf(...)) }."
            )
        }
    }

    override fun configure(data: MavenPublication) = data.pom {
        val pomData = this@PomDataConfigurer
        name.set(pomData.name)
        description.set(pomData.description)
        url.setIfNotBlank(pomData.url)

        licenses {
            pomData.licenses.forEach { license ->
                license {
                    name.set(license.name)
                    url.setIfNotBlank(license.url)
                }
            }
        }
        developers {
            pomData.developers.forEach { developer ->
                developer {
                    id.setIfNotBlank(developer.id)
                    name.set(developer.name)
                    email.setIfNotBlank(developer.email)
                }
            }
        }
        val scmData = pomData.scm
        if (scmData != Scm.default) {
            scm {
                connection.setIfNotBlank(scmData.connection)
                developerConnection.setIfNotBlank(scmData.developerConnection)
                url.setIfNotBlank(scmData.url)
            }
        }
    }

    private fun Property<String>.setIfNotBlank(value: String) {
        if (value.isNotBlank()) set(value)
    }
}
