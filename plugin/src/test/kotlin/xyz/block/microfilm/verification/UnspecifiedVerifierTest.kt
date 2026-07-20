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
import xyz.block.microfilm.ImageGroupFixtures.EMPTY_IMAGE_GROUP
import xyz.block.microfilm.ImageGroupFixtures.KEY
import xyz.block.microfilm.ImageGroupFixtures.LOSSY_MANIFEST_ENTRY
import xyz.block.microfilm.ImageGroupFixtures.MICROFILM_PNG
import xyz.block.microfilm.ImageGroupFixtures.RESOURCES_PNG
import xyz.block.microfilm.ImageGroupFixtures.RESOURCES_WEBP
import xyz.block.microfilm.ImageSettings.Unspecified
import xyz.block.microfilm.verification.UnspecifiedVerifier.Failure

class UnspecifiedVerifierTest {
  private val verifier = UnspecifiedVerifier()

  @Test
  fun `verify fails with resources png`() {
    val result =
      verifier.verify(
        imageGroup = EMPTY_IMAGE_GROUP.copy(resourcesPng = RESOURCES_PNG),
        imageSettings = Unspecified,
      )

    assertThat(result).isInstanceOf<Failure>().all {
      isEqualTo(
        Failure(
          key = KEY,
          hasResourcesPng = true,
          hasResourcesWebp = false,
          hasMicrofilmPng = false,
          hasMicrofilmManifestEntry = false,
        )
      )

      prop(Failure::description)
        .isEqualTo(
          """
          Found an image that is covered by neither a `compress` nor an `exclude` rule in the Gradle configuration: drawable/photo
          [✗] There is a PNG in the resources directory when none is expected
          [✓] There is no WebP in the resources directory
          [✓] There is no PNG in the microfilm directory
          [✓] There is no entry in the microfilm manifest
          To fix this failure, add a `compress` or `exclude` rule that covers this image, or delete the image, then run the `compressMicrofilm` task.
          """
            .trimIndent()
        )
    }
  }

  @Test
  fun `verify fails with resources webp`() {
    val result =
      verifier.verify(
        imageGroup = EMPTY_IMAGE_GROUP.copy(resourcesWebp = RESOURCES_WEBP),
        imageSettings = Unspecified,
      )

    assertThat(result).isInstanceOf<Failure>().all {
      isEqualTo(
        Failure(
          key = KEY,
          hasResourcesPng = false,
          hasResourcesWebp = true,
          hasMicrofilmPng = false,
          hasMicrofilmManifestEntry = false,
        )
      )

      prop(Failure::description)
        .isEqualTo(
          """
          Found an image that is covered by neither a `compress` nor an `exclude` rule in the Gradle configuration: drawable/photo
          [✓] There is no PNG in the resources directory
          [✗] There is a WebP in the resources directory when none is expected
          [✓] There is no PNG in the microfilm directory
          [✓] There is no entry in the microfilm manifest
          To fix this failure, add a `compress` or `exclude` rule that covers this image, or delete the image, then run the `compressMicrofilm` task.
          """
            .trimIndent()
        )
    }
  }

  @Test
  fun `verify fails with microfilm png`() {
    val result =
      verifier.verify(
        imageGroup = EMPTY_IMAGE_GROUP.copy(microfilmPng = MICROFILM_PNG),
        imageSettings = Unspecified,
      )

    assertThat(result).isInstanceOf<Failure>().all {
      isEqualTo(
        Failure(
          key = KEY,
          hasResourcesPng = false,
          hasResourcesWebp = false,
          hasMicrofilmPng = true,
          hasMicrofilmManifestEntry = false,
        )
      )

      prop(Failure::description)
        .isEqualTo(
          """
          Found an image that is covered by neither a `compress` nor an `exclude` rule in the Gradle configuration: drawable/photo
          [✓] There is no PNG in the resources directory
          [✓] There is no WebP in the resources directory
          [✗] There is a PNG in the microfilm directory when none is expected
          [✓] There is no entry in the microfilm manifest
          To fix this failure, add a `compress` or `exclude` rule that covers this image, or delete the image, then run the `compressMicrofilm` task.
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
        imageSettings = Unspecified,
      )

    assertThat(result).isInstanceOf<Failure>().all {
      isEqualTo(
        Failure(
          key = KEY,
          hasResourcesPng = false,
          hasResourcesWebp = false,
          hasMicrofilmPng = false,
          hasMicrofilmManifestEntry = true,
        )
      )

      prop(Failure::description)
        .isEqualTo(
          """
          Found an image that is covered by neither a `compress` nor an `exclude` rule in the Gradle configuration: drawable/photo
          [✓] There is no PNG in the resources directory
          [✓] There is no WebP in the resources directory
          [✓] There is no PNG in the microfilm directory
          [✗] There is an entry in the microfilm manifest when none is expected
          To fix this failure, add a `compress` or `exclude` rule that covers this image, or delete the image, then run the `compressMicrofilm` task.
          """
            .trimIndent()
        )
    }
  }
}
