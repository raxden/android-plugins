package com.raxdenstudios.versioning.version

/**
 * A parsed version from `version.properties`. [versionName] is `MAJOR.MINOR.PATCH`; [dev] only
 * appears in [versionCode], which packs the parts as
 * `MAJOR * 1_000_000 + MINOR * 10_000 + PATCH * 100 + DEV`. MINOR, PATCH and DEV are limited to
 * 0..99 so that the packed code stays unique.
 */
data class Version(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val dev: Int,
) {

    val versionName: String
        get() = "$major.$minor.$patch"

    val versionCode: Int
        get() = computeVersionCode().toInt()

    /**
     * Returns a copy with [part] increased by one and the parts less significant than it reset to 0.
     * Does not validate the result; check it with [validationErrors].
     */
    fun increase(part: VersionPart): Version = when (part) {
        VersionPart.MAJOR -> Version(major = major + 1, minor = 0, patch = 0, dev = 0)
        VersionPart.MINOR -> copy(minor = minor + 1, patch = 0, dev = 0)
        VersionPart.PATCH -> copy(patch = patch + 1, dev = 0)
        VersionPart.DEV -> copy(dev = dev + 1)
    }

    /**
     * Returns the reasons why this version cannot be used, or an empty list when it is valid.
     * Checks that MAJOR is not negative and that MINOR, PATCH and DEV are within 0..99. The
     * [MAX_VERSION_CODE] check only runs when those pass.
     */
    fun validationErrors(): List<String> = buildList {
        if (major < 0) add("MAJOR must not be negative (was $major)")
        listOf(VersionPart.MINOR to minor, VersionPart.PATCH to patch, VersionPart.DEV to dev)
            .forEach { (part, value) ->
                if (value !in 0 until PART_LIMIT) {
                    add("${part.key} must be between 0 and ${PART_LIMIT - 1} (was $value)")
                }
            }
        if (isEmpty() && computeVersionCode() > MAX_VERSION_CODE) {
            add("versionCode ${computeVersionCode()} exceeds Google Play's maximum of $MAX_VERSION_CODE")
        }
    }

    override fun toString(): String = "$versionName(${computeVersionCode()})"

    private fun computeVersionCode(): Long =
        major.toLong() * MAJOR_FACTOR + minor * MINOR_FACTOR + patch * PATCH_FACTOR + dev

    companion object {
        /** Google Play's upper limit for `versionCode`. */
        const val MAX_VERSION_CODE = 2_100_000_000L
        private const val MAJOR_FACTOR = 1_000_000L
        private const val MINOR_FACTOR = 10_000L
        private const val PATCH_FACTOR = 100L
        private const val PART_LIMIT = 100
    }
}
