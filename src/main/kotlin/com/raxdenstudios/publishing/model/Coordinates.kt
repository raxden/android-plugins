package com.raxdenstudios.publishing.model

/**
 * Maven coordinates of a publication. Blank values fall back to the project group, name and
 * version. `Coordinates.default` has every value blank.
 *
 * @property groupId Maven groupId, e.g. `com.raxdenstudios`.
 * @property artifactId Maven artifactId; blank uses the project name.
 * @property version Published version; blank uses the project version.
 */
data class Coordinates(
    val groupId: String,
    val artifactId: String,
    val version: String
) {

    companion object {
        val default = Coordinates(
            groupId = "",
            artifactId = "",
            version = ""
        )
    }
}
