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
import xyz.block.microfilm.ImageSettings.Compress
import xyz.block.microfilm.Manifest.Entry
import xyz.block.microfilm.compression.Compressor.Result
import xyz.block.microfilm.compression.Compressor.Result.Success
import xyz.block.microfilm.cwebp.Cwebp
import xyz.block.microfilm.replaceExtension
import xyz.block.microfilm.scanning.ImageGroup
import xyz.block.microfilm.sha256
import xyz.block.microfilm.toCompressor

/** A [Compressor] that handles image groups covered by [Compress] image settings. */
internal class CompressCompressor(
  private val cwebp: Cwebp,
  private val fileSystem: FileSystem,
  private val resourcesDirectory: Path,
  private val microfilmDirectory: Path,
) : Compressor<Compress> {
  override fun compress(imageGroup: ImageGroup, imageSettings: Compress): Result {
    val cwebpVersion = cwebp.getVersion()
    val resourcesPng = imageGroup.resourcesPng
    var resourcesWebp = imageGroup.resourcesWebp
    var microfilmPng = imageGroup.microfilmPng
    val microfilmManifestEntry = imageGroup.microfilmManifestEntry

    // If there's a resources png, move it to the microfilm directory
    resourcesPng?.let {
      val relativePng = resourcesPng.relativeTo(other = resourcesDirectory)
      microfilmPng = microfilmDirectory.resolve(child = relativePng)
      microfilmPng.parent?.let { parent -> fileSystem.createDirectories(dir = parent) }
      fileSystem.atomicMove(source = resourcesPng, target = microfilmPng)
    }

    // If there is no microfilm png source image, return an empty microfilm manifest entry
    if (microfilmPng == null) {
      resourcesWebp?.let { fileSystem.delete(path = resourcesWebp) }
      return Success(microfilmManifestEntry = null)
    }

    // If the image has already been compressed, return the current microfilm manifest entry
    if (
      resourcesWebp != null &&
        microfilmManifestEntry != null &&
        fileSystem.exists(path = microfilmPng) &&
        fileSystem.exists(path = resourcesWebp) &&
        fileSystem.sha256(file = microfilmPng) == microfilmManifestEntry.sourceSha256 &&
        fileSystem.sha256(file = resourcesWebp) == microfilmManifestEntry.compressedSha256 &&
        imageSettings.toCompressor(cwebpVersion = cwebpVersion) == microfilmManifestEntry.compressor
    ) {
      return Success(microfilmManifestEntry = microfilmManifestEntry)
    }

    // Compress the image
    val relativePng = microfilmPng.relativeTo(other = microfilmDirectory)
    val relativeWebp = relativePng.replaceExtension(old = "png", new = "webp")
    resourcesWebp = resourcesDirectory.resolve(child = relativeWebp)
    resourcesWebp.parent?.let { parent -> fileSystem.createDirectories(dir = parent) }
    cwebp.compress(
      imageSettings = imageSettings,
      sourcePng = microfilmPng,
      destinationWebp = resourcesWebp,
    )

    // Return a new microfilm manifest entry
    return Success(
      microfilmManifestEntry =
        Entry(
          sourcePath = microfilmPng.relativeTo(other = microfilmDirectory).toString(),
          sourceSha256 = fileSystem.sha256(file = microfilmPng),
          compressedPath = resourcesWebp.relativeTo(other = resourcesDirectory).toString(),
          compressedSha256 = fileSystem.sha256(file = resourcesWebp),
          compressor = imageSettings.toCompressor(cwebpVersion = cwebpVersion),
        )
    )
  }
}
