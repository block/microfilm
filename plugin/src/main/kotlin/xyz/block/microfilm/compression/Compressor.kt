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

import okio.Path
import okio.Path.Companion.toPath
import xyz.block.microfilm.ImageRule
import xyz.block.microfilm.ImageSettings
import xyz.block.microfilm.ImageSettings.Exclude
import xyz.block.microfilm.ImageSettings.Unspecified
import xyz.block.microfilm.Manifest
import xyz.block.microfilm.compression.Compressor.Result
import xyz.block.microfilm.resolve
import xyz.block.microfilm.scanning.ImageGroup
import xyz.block.microfilm.scanning.Scanner

/**
 * A compressor that compresses images according to the image settings declared in the Gradle
 * configuration.
 */
internal interface Compressor<T : ImageSettings> {
  /** Compresses the given image using the given settings. */
  fun compress(imageGroup: ImageGroup, imageSettings: T): Result

  sealed interface Result {
    /**
     * Returned when an image was successfully compressed, with a manifest entry that matches the
     * current state. If the image is excluded, the manifest entry is null.
     */
    data class Success(val microfilmManifestEntry: Manifest.Entry?) : Result

    /** Returned when an image could not be compressed for some reason. */
    sealed interface Failure : Result {
      val description: String
    }
  }
}

/** Scans for images, pairs them with rules from the Gradle configuration, and compresses them. */
internal fun Compressor<ImageSettings>.compress(
  scanner: Scanner,
  imageRules: List<ImageRule>,
  resourcesDirectory: Path,
  microfilmDirectory: Path,
): List<Result> {
  return scanner.scan().map { imageGroup ->
    val pngPath =
      imageGroup.microfilmManifestEntry?.sourcePath?.toPath()
        ?: imageGroup.microfilmPng?.relativeTo(other = microfilmDirectory)
        ?: imageGroup.resourcesPng?.relativeTo(other = resourcesDirectory)
    val webpPath = imageGroup.resourcesWebp?.relativeTo(other = resourcesDirectory)
    val imageSettings =
      when {
        pngPath != null -> imageRules.resolve(imagePath = pngPath)?.imageSettings

        webpPath != null ->
          imageRules.resolve(imagePath = webpPath)?.imageSettings?.takeIf { it is Exclude }

        else -> null
      } ?: Unspecified
    compress(imageGroup = imageGroup, imageSettings = imageSettings)
  }
}
