package com.raxdenstudios.publishing.configurer

import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

class PomDataConfigurer(
    private val project: Project,
    extensionName: String,
) : PublishingConfigurer<MavenPublication>(project, extensionName) {

    private val pom = extension.pom

    private val name: String
        get() = pom.name
            .ifBlank { extension.name }
            .ifBlank { project.name }

    private val description
        get() = pom.description
            .ifBlank { extension.description }
            .ifBlank { project.description.orEmpty() }
            .ifBlank { "Publication for ${project.group}:${project.name}" }

    private val url
        get() = pom.url
            .ifBlank { extension.url }
            .ifBlank { error("Project URL is required for publication '${project.path}'.") }

    private val licenses
        get() = pom.licenses

    private val developers
        get() = pom.developers
            .ifEmpty { listOf(extension.developer) }

    private val scmConnection: String
        get() = pom.scm.connection
            .ifBlank { "scm:git:${url.removePrefix("https://")}.git" }

    private val scmDeveloperConnection: String
        get() = pom.scm.developerConnection
            .ifBlank { "scm:git:ssh://${url.removePrefix("https://")}.git" }

    private val scmUrl: String
        get() = pom.scm.url
            .ifBlank { "$url/tree/master" }

    override fun configure(data: MavenPublication) = data.run {
        pom {
            name.set(this@PomDataConfigurer.name)
            description.set(this@PomDataConfigurer.description)
            url.set(this@PomDataConfigurer.url)

            licenses {
                this@PomDataConfigurer.licenses.forEach { license ->
                    license {
                        name.set(license.name)
                        url.set(license.url)
                    }
                }
            }
            developers {
                this@PomDataConfigurer.developers.forEach { developer ->
                    developer {
                        id.set(developer.id)
                        name.set(developer.name)
                        email.set(developer.email)
                    }
                }
            }
            scm {
                connection.set(this@PomDataConfigurer.scmConnection)
                developerConnection.set(this@PomDataConfigurer.scmDeveloperConnection)
                url.set(this@PomDataConfigurer.scmUrl)
            }
        }
    }
}
