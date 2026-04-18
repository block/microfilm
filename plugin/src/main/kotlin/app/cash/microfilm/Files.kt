package app.cash.microfilm

import java.io.File
import java.security.MessageDigest

/** Produces the SHA256 hash of the given file. */
fun File.sha256(): String {
  val digest = MessageDigest.getInstance("SHA-256")
  inputStream().use { input ->
    val buffer = ByteArray(8192)
    var bytesRead: Int
    while (input.read(buffer).also { bytesRead = it } != -1) {
      digest.update(buffer, 0, bytesRead)
    }
  }
  return digest.digest().joinToString("") { "%02x".format(it) }
}
