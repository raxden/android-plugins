package com.raxdenstudios.publishing.configurer

import com.raxdenstudios.getOrCreateExtension
import com.raxdenstudios.publishing.PublishLibraryPlugin
import com.raxdenstudios.publishing.extension.PublishExtension
import org.gradle.api.Project

abstract class PublishingConfigurer<T: Any>(
    private val project: Project,
    private val extensionName: String,
) {

    protected val extension: PublishExtension by lazy {
        project.getOrCreateExtension<PublishExtension>(extensionName)
    }

    abstract fun configure(data: T)
}
