/*
 * Copyright (C) 2026 Block, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.block.microfilm.compression

import okio.FileSystem
import okio.Path
import xyz.block.microfilm.ImageSettings.Unspecified
import xyz.block.microfilm.appendCheckboxLine
import xyz.block.microfilm.compression.Compressor.Result
import xyz.block.microfilm.compression.Compressor.Result.Success
import xyz.block.microfilm.scanning.ImageGroup

/** A [Compressor] that handles image groups covered by [Unspecified] image settings. */
internal class UnspecifiedCompressor(
  private val fileSystem: FileSystem,
  private val resourcesDirectory: Path,
  private val microfilmDirectory: Path,
) : Compressor<Unspecified> {
  override fun compress(imageGroup: ImageGroup, imageSettings: Unspecified): Result {
    val hasResourcesPng = imageGroup.resourcesPng != null
    val hasResourcesWebp = imageGroup.resourcesWebp != null
    val hasMicrofilmPng = imageGroup.microfilmPng != null

    when {
      // If there is a microfilm png but no resources png, restore the resources png
      hasMicrofilmPng && !hasResourcesPng -> {
        val relativePng = imageGroup.microfilmPng.relativeTo(other = microfilmDirectory)
        val resourcesPng = resourcesDirectory.resolve(child = relativePng)
        resourcesPng.parent?.let { parent -> fileSystem.createDirectories(dir = parent) }
        fileSystem.atomicMove(source = imageGroup.microfilmPng, target = resourcesPng)
      }

      // If there is both a microfilm png and a resources png, delete the microfilm png
      hasMicrofilmPng && hasResourcesPng -> {
        fileSystem.delete(path = imageGroup.microfilmPng)
      }
    }

    return if (!hasResourcesPng && !hasResourcesWebp && !hasMicrofilmPng) {
      Success(microfilmManifestEntry = null)
    } else {
      Failure(
        key = imageGroup.key,
        hasResourcesPng = hasResourcesPng || hasMicrofilmPng,
        hasResourcesWebp = hasResourcesWebp,
      )
    }
  }

  data class Failure(val key: String, val hasResourcesPng: Boolean, val hasResourcesWebp: Boolean) :
    Result.Failure {
    override val description = buildString {
      appendLine(
        "Found an image that is covered by neither a `compress` nor an `exclude` rule in the Gradle configuration: $key"
      )

      appendCheckboxLine(
        isCorrect = !hasResourcesPng,
        correctValue = "There is no PNG in the resources directory",
        incorrectValue = "There is a PNG in the resources directory when none is expected",
      )

      appendCheckboxLine(
        isCorrect = !hasResourcesWebp,
        correctValue = "There is no WebP in the resources directory",
        incorrectValue = "There is a WebP in the resources directory when none is expected",
      )

      append(
        "To fix this failure, add a `compress` or `exclude` rule that covers this image, or delete the image, then rerun the `compressMicrofilm` task."
      )
    }
  }
}
