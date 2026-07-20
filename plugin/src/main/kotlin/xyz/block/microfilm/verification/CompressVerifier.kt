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

import okio.FileSystem
import xyz.block.microfilm.ImageSettings.Compress
import xyz.block.microfilm.appendCheckboxLine
import xyz.block.microfilm.cwebp.Cwebp
import xyz.block.microfilm.scanning.ImageGroup
import xyz.block.microfilm.sha256
import xyz.block.microfilm.toCompressor
import xyz.block.microfilm.verification.Verifier.Result
import xyz.block.microfilm.verification.Verifier.Result.Success

internal class CompressVerifier(private val cwebp: Cwebp, private val fileSystem: FileSystem) :
  Verifier<Compress> {
  override fun verify(imageGroup: ImageGroup, imageSettings: Compress): Result {
    val hasResourcesPng = imageGroup.resourcesPng != null
    val hasResourcesWebp = imageGroup.resourcesWebp != null
    val hasMicrofilmPng = imageGroup.microfilmPng != null
    val hasMicrofilmManifestEntry = imageGroup.microfilmManifestEntry != null

    val hasCorrectResourcesWebpHash =
      hasResourcesWebp &&
        hasMicrofilmManifestEntry &&
        fileSystem.sha256(file = imageGroup.resourcesWebp) ==
          imageGroup.microfilmManifestEntry.compressedSha256
    val hasCorrectMicrofilmPngHash =
      hasMicrofilmPng &&
        hasMicrofilmManifestEntry &&
        fileSystem.sha256(file = imageGroup.microfilmPng) ==
          imageGroup.microfilmManifestEntry.sourceSha256
    val hasCorrectCompressionSettings =
      hasMicrofilmManifestEntry &&
        imageSettings.toCompressor(cwebpVersion = cwebp.getVersion()) ==
          imageGroup.microfilmManifestEntry.compressor

    return if (
      !hasResourcesPng &&
        hasCorrectResourcesWebpHash &&
        hasCorrectMicrofilmPngHash &&
        hasCorrectCompressionSettings
    ) {
      Success(key = imageGroup.key)
    } else {
      Failure(
        key = imageGroup.key,
        hasResourcesPng = hasResourcesPng,
        hasResourcesWebp = hasResourcesWebp,
        hasMicrofilmPng = hasMicrofilmPng,
        hasMicrofilmManifestEntry = hasMicrofilmManifestEntry,
        hasCorrectResourcesWebpHash = hasCorrectResourcesWebpHash,
        hasCorrectMicrofilmPngHash = hasCorrectMicrofilmPngHash,
        hasCorrectCompressionSettings = hasCorrectCompressionSettings,
      )
    }
  }

  data class Failure(
    override val key: String,
    val hasResourcesPng: Boolean,
    val hasResourcesWebp: Boolean,
    val hasMicrofilmPng: Boolean,
    val hasMicrofilmManifestEntry: Boolean,
    val hasCorrectResourcesWebpHash: Boolean,
    val hasCorrectMicrofilmPngHash: Boolean,
    val hasCorrectCompressionSettings: Boolean,
  ) : Result.Failure {
    override val description: String = buildString {
      appendLine(
        "Found an image that is covered by a `compress` rule in the Gradle configuration but is not fully compressed: $key"
      )

      appendCheckboxLine(
        isCorrect = !hasResourcesPng,
        correctValue = "There is no PNG in the resources directory",
        incorrectValue = "There is a PNG in the resources directory when none is expected",
      )

      appendCheckboxLine(
        isCorrect = hasResourcesWebp && hasCorrectResourcesWebpHash,
        correctValue = "There is a WebP in the resources directory with the correct hash",
        incorrectValue =
          when {
            !hasResourcesWebp -> "There is no WebP in the resources directory when one is expected"

            !hasMicrofilmManifestEntry ->
              "There is a WebP in the resources directory, but there is no manifest entry to compare its hash against"

            else -> "There is a WebP in the resources directory, but it has an incorrect hash"
          },
      )

      appendCheckboxLine(
        isCorrect = hasMicrofilmPng && hasCorrectMicrofilmPngHash,
        correctValue = "There is a PNG in the microfilm directory with the correct hash",
        incorrectValue =
          when {
            !hasMicrofilmPng -> "There is no PNG in the microfilm directory when one is expected"

            !hasMicrofilmManifestEntry ->
              "There is a PNG in the microfilm directory, but there is no manifest entry to compare its hash against"

            else -> "There is a PNG in the microfilm directory, but it has an incorrect hash"
          },
      )

      appendCheckboxLine(
        isCorrect = hasMicrofilmManifestEntry && hasCorrectCompressionSettings,
        correctValue =
          "There is an entry in the microfilm manifest, with the correct compression settings",
        incorrectValue =
          if (!hasMicrofilmManifestEntry) {
            "There is no entry in the microfilm manifest when one is expected"
          } else {
            "There is an entry in the microfilm manifest, but it has incorrect compression settings"
          },
      )

      append("To fix this failure, run the `compressMicrofilm` task.")
    }
  }
}
