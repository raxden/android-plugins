package com.raxdenstudios.publishing.configurer

import com.raxdenstudios.getOrCreateExtension
import com.raxdenstudios.publishing.extension.PublishExtension
import org.gradle.api.Project

/**
 * Base of the configurers that feed one part of a publication from the [PublishExtension] named
 * [extensionName]. The extension is looked up lazily, so a configurer can be created before the
 * build script has set its values.
 */
abstract class PublishingConfigurer<T : Any>(
    private val project: Project,
    private val extensionName: String,
) {

    protected val extension: PublishExtension by lazy {
        project.getOrCreateExtension<PublishExtension>(extensionName)
    }

    /** Applies the configuration to [data]. Called once the build script has been evaluated. */
    abstract fun configure(data: T)
}
