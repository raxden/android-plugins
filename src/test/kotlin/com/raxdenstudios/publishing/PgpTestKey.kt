package com.raxdenstudios.publishing

import org.bouncycastle.bcpg.ArmoredOutputStream
import org.bouncycastle.bcpg.HashAlgorithmTags
import org.bouncycastle.openpgp.PGPEncryptedData
import org.bouncycastle.openpgp.PGPKeyRingGenerator
import org.bouncycastle.openpgp.PGPPublicKey
import org.bouncycastle.openpgp.PGPSignature
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyPair
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyEncryptorBuilder
import java.io.ByteArrayOutputStream
import java.security.KeyPairGenerator
import java.util.Date

/** Throwaway ASCII-armored PGP secret key generated in memory, never a real signing key. */
internal class PgpTestKey private constructor(
    val armoredSecretKey: String,
    val keyId: String,
    val password: String,
) {

    companion object {

        private const val RSA_KEY_SIZE = 2048

        fun generate(password: String = "throwaway-password"): PgpTestKey {
            val rsa = KeyPairGenerator.getInstance("RSA").apply { initialize(RSA_KEY_SIZE) }
            val keyPair = JcaPGPKeyPair(PGPPublicKey.RSA_GENERAL, rsa.generateKeyPair(), Date())
            val sha1 = JcaPGPDigestCalculatorProviderBuilder().build().get(HashAlgorithmTags.SHA1)
            val ringGenerator = PGPKeyRingGenerator(
                PGPSignature.POSITIVE_CERTIFICATION,
                keyPair,
                "Throwaway <throwaway@example.com>",
                sha1,
                null,
                null,
                JcaPGPContentSignerBuilder(keyPair.publicKey.algorithm, HashAlgorithmTags.SHA256),
                JcePBESecretKeyEncryptorBuilder(PGPEncryptedData.AES_256, sha1).build(password.toCharArray()),
            )
            val bytes = ByteArrayOutputStream()
            ArmoredOutputStream(bytes).use { armored -> ringGenerator.generateSecretKeyRing().encode(armored) }
            return PgpTestKey(
                armoredSecretKey = bytes.toString(Charsets.US_ASCII),
                keyId = "%08X".format(keyPair.keyID and 0xFFFFFFFFL),
                password = password,
            )
        }
    }
}
