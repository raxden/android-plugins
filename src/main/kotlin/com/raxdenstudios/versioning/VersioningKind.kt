package com.raxdenstudios.versioning

/**
 * What the versioned module publishes: an app ([APP], versionName + versionCode) or a
 * library/platform ([LIBRARY], version only).
 *
 * Dynamic feature modules are registered as [APP] so they get the same tasks, but their
 * versionName and versionCode come from the base app, so this plugin does not set them.
 */
enum class VersioningKind {
    APP,
    LIBRARY,
}
