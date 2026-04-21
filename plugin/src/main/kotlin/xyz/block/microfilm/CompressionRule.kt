package xyz.block.microfilm

import java.io.Serializable
import java.nio.file.FileSystems
import java.nio.file.Path

data class CompressionRule(val pattern: String, val compressionSettings: CompressionSettings) :
  Serializable

/** Returns true if the [CompressionRule] matches the given image. */
internal fun CompressionRule.matches(imagePath: String): Boolean =
  FileSystems.getDefault().getPathMatcher("glob:$pattern").matches(Path.of(imagePath))

/** Returns the last [CompressionRule] that applies to the given image. */
internal fun List<CompressionRule>.resolve(imagePath: String): CompressionRule =
  last { compressionRule ->
    compressionRule.matches(imagePath)
  }
