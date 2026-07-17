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

import xyz.block.microfilm.ImageSettings.Exclude
import xyz.block.microfilm.appendCheckboxLine
import xyz.block.microfilm.scanning.ImageGroup
import xyz.block.microfilm.verification.Verifier.Result
import xyz.block.microfilm.verification.Verifier.Result.Success

/** A [Verifier] that handles image groups covered by [Exclude] image settings. */
internal class ExcludeVerifier : Verifier<Exclude> {
  override fun verify(imageGroup: ImageGroup, imageSettings: Exclude): Result {
    val hasMicrofilmPng = imageGroup.microfilmPng != null
    val hasMicrofilmManifestEntry = imageGroup.microfilmManifestEntry != null

    return if (!hasMicrofilmPng && !hasMicrofilmManifestEntry) {
      Success(key = imageGroup.key)
    } else {
      Failure(
        key = imageGroup.key,
        hasMicrofilmPng = hasMicrofilmPng,
        hasMicrofilmManifestEntry = hasMicrofilmManifestEntry,
      )
    }
  }

  data class Failure(
    override val key: String,
    val hasMicrofilmPng: Boolean,
    val hasMicrofilmManifestEntry: Boolean,
  ) : Result.Failure {
    override val description = buildString {
      appendLine(
        "Found an image that is covered by an `exclude` rule in the Gradle configuration but is not fully excluded: $key"
      )

      appendCheckboxLine(
        isCorrect = !hasMicrofilmPng,
        correctValue = "There is no PNG in the microfilm directory",
        incorrectValue = "There is a PNG in the microfilm directory when none is expected",
      )

      appendCheckboxLine(
        isCorrect = !hasMicrofilmManifestEntry,
        correctValue = "There is no entry in the microfilm manifest",
        incorrectValue = "There is an entry in the microfilm manifest when none is expected",
      )

      append("To fix this failure, run the `compressMicrofilm` task.")
    }
  }
}
