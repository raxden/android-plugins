package com.raxdenstudios.publishing.extension

import com.raxdenstudios.publishing.model.Coordinates
import com.raxdenstudios.publishing.model.Developer
import com.raxdenstudios.publishing.model.Pom
import com.raxdenstudios.publishing.model.Repository
import com.raxdenstudios.publishing.model.Signing
import org.gradle.api.Project
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * DSL of the `publishLibrary` / `publishPlatform` extensions, one type for both plugins.
 *
 * Every value is a lazy [Property], read by the plugin only once the build script has been
 * evaluated, so the order of the assignments does not matter. In Kotlin DSL scripts values are
 * still assigned with `=` (e.g. `url = "https://github.com/..."`). A value is final once the
 * plugin has read it: assigning it afterwards fails.
 *
 * Blank values fall back to project data or to Gradle properties / environment variables, as
 * documented on each property. See [com.raxdenstudios.publishing.PublishLibraryPlugin] for a
 * usage example.
 */
abstract class PublishExtension @Inject constructor(
    val project: Project
) {

    /** Name of the POM. When blank, the project name is used. */
    abstract val name: Property<String>

    /** Description of the POM. When blank, the project description or a generic text is used. */
    abstract val description: Property<String>

    /**
     * Project home page, written to the POM and used to derive the SCM block. Required: it must be
     * an `http(s)://` url, otherwise POM generation fails.
     */
    abstract val url: Property<String>

    /** Developer written to the POM when [pom] lists none. Required: its name must not be blank. */
    abstract val developer: Property<Developer>

    /** Maven groupId, artifactId and version. Blank values fall back to the project group, name and version. */
    abstract val coordinates: Property<Coordinates>

    /** Extra POM data (licenses, developers, SCM). Its values take precedence over the properties above. */
    abstract val pom: Property<Pom>

    /**
     * Remote Maven repository to publish to. No repository is added while its url is blank (the
     * default), which leaves publishing to tools such as NMCP that consume the publications.
     */
    abstract val repository: Property<Repository>

    /**
     * In-memory PGP signing key. Blank values fall back to the `SIGNING_KEY_ID`, `SIGNING_KEY` and
     * `SIGNING_PASSWORD` Gradle properties or environment variables.
     */
    abstract val signing: Property<Signing>

    init {
        name.finalConvention("")
        description.finalConvention("")
        url.finalConvention("")
        developer.finalConvention(Developer.default)
        coordinates.finalConvention(Coordinates.default)
        pom.finalConvention(Pom.default)
        repository.finalConvention(Repository.default)
        signing.finalConvention(Signing.default)
    }

    private fun <T : Any> Property<T>.finalConvention(value: T) {
        convention(value)
        finalizeValueOnRead()
    }
}
