package com.raxdenstudios.publishing.configurer

import com.raxdenstudios.publishing.PgpTestKey
import com.raxdenstudios.publishing.model.Signing
import org.gradle.plugins.signing.SigningExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class SigningDataConfigurerTest {

    @TempDir
    lateinit var tempDir: Path

    private val fixture by lazy { ConfigurerTestProject(tempDir) }

    private val key by lazy { PgpTestKey.generate() }

    private val signingExtension: SigningExtension
        get() = fixture.project.extensions.getByType(SigningExtension::class.java)

    private fun configure() {
        fixture.publication()
        SigningDataConfigurer(fixture.project, ConfigurerTestProject.EXTENSION_NAME).configure(signingExtension)
    }

    @Test
    fun `Given no key When configuring Then no signatory is available`() {
        configure()

        assertNull(signingExtension.signatory)
    }

    @Test
    fun `Given a key without key id When configuring Then the two argument key is used`() {
        fixture.extension.signing.set(Signing(keyId = "", key = key.armoredSecretKey, password = key.password))

        configure()

        assertNotNull(signingExtension.signatory)
    }

    @Test
    fun `Given a key with key id When configuring Then the three argument key is used`() {
        fixture.extension.signing.set(Signing(keyId = key.keyId, key = key.armoredSecretKey, password = key.password))

        configure()

        assertNotNull(signingExtension.signatory)
    }

    @Test
    fun `Given a password but no key When configuring Then nothing is signed`() {
        fixture.extension.signing.set(Signing(keyId = "ABCD1234", key = "", password = "secret"))

        configure()

        assertNull(signingExtension.signatory)
    }

    @Test
    fun `Given publications When configuring Then a sign task is created for each one`() {
        configure()

        assertTrue(fixture.project.tasks.names.contains("signPlatformPublication"))
    }

    @Test
    fun `Given no repository publishing task in the graph When configuring Then signing is not required`() {
        configure()

        assertFalse(signingExtension.isRequired)
    }

    @Test
    fun `Given a key with a blank password When configuring Then the password is looked up and signing is set up`() {
        val unprotected = PgpTestKey.generate(password = "")
        fixture.extension.signing.set(Signing(keyId = "", key = unprotected.armoredSecretKey, password = ""))

        configure()

        assertTrue(fixture.project.tasks.names.contains("signPlatformPublication"))
    }
}
