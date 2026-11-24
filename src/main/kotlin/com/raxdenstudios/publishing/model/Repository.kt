package com.raxdenstudios.publishing.model

/**
 * Remote Maven repository to publish to. `Repository.default` has a blank [url], which adds no
 * repository at all. Publishing to the Maven Central Portal does not need one (see the
 * deprecation note on [sonatype]).
 *
 * @property name Name of the Maven repository in Gradle; blank leaves it unset.
 * @property url Repository url; blank adds no repository.
 * @property credentials Username and password; blank values fall back to Gradle properties or
 * environment variables.
 */
data class Repository(
    val name: String,
    val url: String,
    val credentials: Credentials
) {

    companion object {
        val default = Repository(
            name = "",
            url = "",
            credentials = Credentials.default
        )

        /**
         * The former OSSRH staging repository. Its host no longer accepts deployments, so it must
         * not be used; it is kept only so existing references still compile.
         */
        @Deprecated(
            message = "OSSRH (oss.sonatype.org) was shut down in June 2025. Publish to the " +
                "Maven Central Portal instead, e.g. with the NMCP plugin (com.gradleup.nmcp), " +
                "which consumes the maven-publish publications without any repository.",
        )
        val sonatype = Repository(
            name = "sonatype",
            url = "https://oss.sonatype.org/service/local/staging/deploy/maven2/",
            credentials = Credentials.default
        )
    }
}
