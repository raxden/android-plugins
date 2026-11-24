package com.raxdenstudios.publishing.model

/**
 * Source control information written to the POM. Blank values are omitted, or derived from the
 * project url by [withUrl].
 *
 * @property connection Read-only connection, e.g. `scm:git:https://github.com/owner/repo.git`.
 * @property developerConnection Read-write connection, e.g. `scm:git:ssh://git@github.com/owner/repo.git`.
 * @property url Browsable url of the repository.
 */
data class Scm(
    val connection: String,
    val developerConnection: String,
    val url: String
) {

    companion object {
        val default = Scm(
            connection = "",
            developerConnection = "",
            url = ""
        )

        /**
         * Derives the SCM block from the project [url] (e.g. `https://github.com/owner/repo`):
         * - connection: `scm:git:https://github.com/owner/repo.git`
         * - developerConnection: `scm:git:ssh://git@github.com/owner/repo.git`
         * - url: the project url, trimmed and without a trailing `/` or `.git`.
         *
         * @throws IllegalArgumentException when [url] is not an `http(s)://` url (e.g.
         * `git@github.com:owner/repo`): set the [Scm] values explicitly in that case.
         */
        fun withUrl(url: String): Scm = withUrl(url = url, branch = "")

        /**
         * Same as [withUrl], but the SCM url points to [branch] (`<url>/tree/<branch>`) when it is
         * not blank.
         *
         * @throws IllegalArgumentException when [url] is not an `http(s)://` url.
         */
        fun withUrl(url: String, branch: String): Scm {
            require(isWebUrl(url)) {
                "Cannot derive SCM data from '$url': expected an http(s) url such as " +
                    "'https://github.com/owner/repo'. Set the Scm values explicitly instead."
            }
            val webUrl = url.trim().removeSuffix("/").removeSuffix(".git")
            val host = webUrl.substringAfter("://")
            return Scm(
                connection = "scm:git:$webUrl.git",
                developerConnection = "scm:git:ssh://git@$host.git",
                url = if (branch.isBlank()) webUrl else "$webUrl/tree/$branch"
            )
        }

        /** Whether [url] has an `http://` or `https://` scheme, so [withUrl] can derive from it. */
        fun isWebUrl(url: String): Boolean {
            val trimmed = url.trim()
            return trimmed.startsWith("https://", ignoreCase = true) ||
                trimmed.startsWith("http://", ignoreCase = true)
        }
    }
}
