package com.raxdenstudios.versioning.version

/**
 * The four parts of a version, from most to least significant. [key] is the property name in
 * `version.properties`; [label] is the lowercase name used in task output.
 */
enum class VersionPart(val key: String) {
    MAJOR("MAJOR"),
    MINOR("MINOR"),
    PATCH("PATCH"),
    DEV("DEV");

    val label: String
        get() = name.lowercase()
}
