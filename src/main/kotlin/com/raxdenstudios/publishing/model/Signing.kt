package com.raxdenstudios.publishing.model

/**
 * In-memory PGP signing key. Blank values fall back to the `SIGNING_KEY_ID`, `SIGNING_KEY` and
 * `SIGNING_PASSWORD` Gradle properties or environment variables. [keyId] is optional.
 *
 * @property keyId Id of the signing key; blank uses the key without an explicit id.
 * @property key ASCII-armored PGP secret key.
 * @property password Passphrase of [key].
 */
data class Signing(
    val keyId: String,
    val key: String,
    val password: String
) {

    companion object {
        val default = Signing(
            keyId = "",
            key = "",
            password = ""
        )
    }
}
