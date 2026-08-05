package ktor.oauth2.multi.tenant.auth.server.utils

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object PkceUtils {
    private const val VERIFIER_LENGTH = 64

    fun generateCodeVerifier(): String {
        val random = SecureRandom()
        val bytes = ByteArray(VERIFIER_LENGTH)
        random.nextBytes(bytes)

        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(bytes)
    }

    fun generateCodeChallengeS256(codeVerifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(codeVerifier.toByteArray(Charsets.US_ASCII))

        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(hash)
    }
}
