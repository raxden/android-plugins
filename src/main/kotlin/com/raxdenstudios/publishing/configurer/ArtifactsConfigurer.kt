package com.raxdenstudios.publishing.configurer

import com.android.build.api.dsl.LibraryExtension

/** Publishes the `release` variant of an Android library with sources and javadoc jars. */
class ArtifactsConfigurer {

    companion object {
        private const val RELEASE = "release"
    }

    /** Declares the `release` variant as a single publishing variant, with sources and javadoc jars. */
    fun configure(data: LibraryExtension) = data.run {
        publishing {
            singleVariant(RELEASE) {
                withSourcesJar()
                withJavadocJar()
            }
        }
    }
}
