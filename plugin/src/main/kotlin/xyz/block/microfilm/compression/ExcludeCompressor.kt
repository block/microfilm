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
import xyz.block.microfilm.ImageSettings.Exclude
import xyz.block.microfilm.compression.Compressor.Result
import xyz.block.microfilm.compression.Compressor.Result.Success
import xyz.block.microfilm.scanning.ImageGroup

/** A [Compressor] that handles image groups covered by [Exclude] image settings. */
internal class ExcludeCompressor(
  private val fileSystem: FileSystem,
  private val resourcesDirectory: Path,
  private val microfilmDirectory: Path,
) : Compressor<Exclude> {
  override fun compress(imageGroup: ImageGroup, imageSettings: Exclude): Result {
    val hasResourcesPng = imageGroup.resourcesPng != null
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

    // Return an empty microfilm manifest entry
    return Success(microfilmManifestEntry = null)
  }
}
