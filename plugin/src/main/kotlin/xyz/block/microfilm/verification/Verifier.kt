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
package xyz.block.microfilm.verification

import okio.Path
import okio.Path.Companion.toPath
import xyz.block.microfilm.ImageRule
import xyz.block.microfilm.ImageSettings
import xyz.block.microfilm.ImageSettings.Exclude
import xyz.block.microfilm.ImageSettings.Unspecified
import xyz.block.microfilm.resolve
import xyz.block.microfilm.scanning.ImageGroup
import xyz.block.microfilm.scanning.Scanner
import xyz.block.microfilm.verification.Verifier.Result

/**
 * A verifier that checks images against the image settings declared in the Gradle configuration.
 */
internal interface Verifier<T : ImageSettings> {
  /** Verifies the given image using the given settings . */
  fun verify(imageGroup: ImageGroup, imageSettings: T): Result

  sealed interface Result {
    /** The key of the [ImageGroup] that was verified. */
    val key: String

    /** Returned when an image passes verification. */
    data class Success(override val key: String) : Result

    /** Returned when an image fails verification for some reason. */
    sealed interface Failure : Result {
      val description: String
    }
  }
}

/**
 * Reads the relevant contents of the file system (resources PNGs, resources WebPs, microfilm PNGs,
 * microfilm manifest entries), matches them up with the given image compression rules, verifies
 * everything, and returns any errors that it finds.
 */
internal fun Verifier<ImageSettings>.verify(
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
    verify(imageGroup = imageGroup, imageSettings = imageSettings)
  }
}
