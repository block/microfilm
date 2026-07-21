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

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import org.junit.jupiter.api.Test
import xyz.block.microfilm.ImageSettings.Exclude
import xyz.block.microfilm.scanning.ImageGroupFixtures.EMPTY_IMAGE_GROUP
import xyz.block.microfilm.scanning.ImageGroupFixtures.KEY
import xyz.block.microfilm.scanning.ImageGroupFixtures.LOSSY_MANIFEST_ENTRY
import xyz.block.microfilm.scanning.ImageGroupFixtures.MICROFILM_PNG
import xyz.block.microfilm.scanning.ImageGroupFixtures.RESOURCES_PNG
import xyz.block.microfilm.scanning.ImageGroupFixtures.RESOURCES_WEBP
import xyz.block.microfilm.verification.ExcludeVerifier.Failure
import xyz.block.microfilm.verification.Verifier.Result.Success

class ExcludeVerifierTest {
  private val verifier = ExcludeVerifier()

  @Test
  fun `verify succeeds with resources png`() {
    val result =
      verifier.verify(
        imageGroup = EMPTY_IMAGE_GROUP.copy(resourcesPng = RESOURCES_PNG),
        imageSettings = Exclude,
      )

    assertThat(result).isEqualTo(Success(key = KEY))
  }

  @Test
  fun `verify succeeds with resources webp`() {
    val result =
      verifier.verify(
        imageGroup = EMPTY_IMAGE_GROUP.copy(resourcesWebp = RESOURCES_WEBP),
        imageSettings = Exclude,
      )

    assertThat(result).isEqualTo(Success(key = KEY))
  }

  @Test
  fun `verify fails with microfilm png`() {
    val result =
      verifier.verify(
        imageGroup = EMPTY_IMAGE_GROUP.copy(microfilmPng = MICROFILM_PNG),
        imageSettings = Exclude,
      )

    assertThat(result).isInstanceOf<Failure>().all {
      isEqualTo(Failure(key = KEY, hasMicrofilmPng = true, hasMicrofilmManifestEntry = false))

      prop(Failure::description)
        .isEqualTo(
          """
          Found an image that is covered by an `exclude` rule in the Gradle configuration but is not fully excluded: drawable/photo
          [✗] There is a PNG in the microfilm directory when none is expected
          [✓] There is no entry in the microfilm manifest
          To fix this failure, run the `compressMicrofilm` task.
          """
            .trimIndent()
        )
    }
  }

  @Test
  fun `verify fails with microfilm manifest entry`() {
    val result =
      verifier.verify(
        imageGroup = EMPTY_IMAGE_GROUP.copy(microfilmManifestEntry = LOSSY_MANIFEST_ENTRY),
        imageSettings = Exclude,
      )

    assertThat(result).isInstanceOf<Failure>().all {
      isEqualTo(Failure(key = KEY, hasMicrofilmPng = false, hasMicrofilmManifestEntry = true))

      prop(Failure::description)
        .isEqualTo(
          """
          Found an image that is covered by an `exclude` rule in the Gradle configuration but is not fully excluded: drawable/photo
          [✓] There is no PNG in the microfilm directory
          [✗] There is an entry in the microfilm manifest when none is expected
          To fix this failure, run the `compressMicrofilm` task.
          """
            .trimIndent()
        )
    }
  }
}
