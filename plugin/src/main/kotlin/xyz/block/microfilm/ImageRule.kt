package xyz.block.microfilm

import java.io.Serializable
import java.nio.file.FileSystems
import java.nio.file.Path

data class ImageRule(val pattern: String, val imageSettings: ImageSettings) : Serializable

/** Returns true if the [ImageRule] matches the given image. */
internal fun ImageRule.matches(imagePath: String): Boolean =
  FileSystems.getDefault().getPathMatcher("glob:$pattern").matches(Path.of(imagePath))

/** Returns the last [ImageRule] that matches the given image, or null if none match. */
internal fun List<ImageRule>.resolve(imagePath: String): ImageRule? = lastOrNull { imageRule ->
  imageRule.matches(imagePath)
}
