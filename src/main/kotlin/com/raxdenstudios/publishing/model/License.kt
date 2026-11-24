package com.raxdenstudios.publishing.model

/**
 * License listed in the POM. Licenses with a blank name are ignored. [apache2] is the Apache
 * License 2.0, which `Pom.default` uses.
 *
 * @property name Display name of the license.
 * @property url Link to the license text; omitted when blank.
 */
data class License(
    val name: String,
    val url: String
) {

    companion object {
        val default = License(
            name = "",
            url = ""
        )
        val apache2 = License(
            name = "The Apache License, Version 2.0",
            url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
        )
    }
}
