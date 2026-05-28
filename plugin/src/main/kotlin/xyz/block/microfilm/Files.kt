package xyz.block.microfilm

import java.io.File
import java.security.MessageDigest

/** Matches Android drawable resource directories like `drawable` and `drawable-hdpi`. */
private val DRAWABLE_DIRECTORY_PATTERN = Regex(pattern = "^drawable(-.*)?$")

/** True if this is a PNG image in a drawable directory. Excludes nine-patch (`.9.png`) files. */
internal val File.isPngDrawable: Boolean
  get() =
    extension.equals("png", ignoreCase = true) &&
      !name.endsWith(".9.png", ignoreCase = true) &&
      DRAWABLE_DIRECTORY_PATTERN.matches(input = parentFile.name)

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
