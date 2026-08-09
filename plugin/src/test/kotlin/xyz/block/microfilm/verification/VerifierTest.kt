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

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import org.junit.jupiter.api.Test
import xyz.block.microfilm.ImageRule
import xyz.block.microfilm.ImageSettings
import xyz.block.microfilm.ImageSettings.Exclude
import xyz.block.microfilm.ImageSettings.Unspecified
import xyz.block.microfilm.scanning.FakeScanner
import xyz.block.microfilm.scanning.ImageGroup
import xyz.block.microfilm.scanning.ImageGroupFixtures.EMPTY_IMAGE_GROUP
import xyz.block.microfilm.scanning.ImageGroupFixtures.LOSSY_COMPRESS
import xyz.block.microfilm.scanning.ImageGroupFixtures.LOSSY_IMAGE_GROUP
import xyz.block.microfilm.scanning.ImageGroupFixtures.LOSSY_MANIFEST_ENTRY
import xyz.block.microfilm.scanning.ImageGroupFixtures.MICROFILM_DIRECTORY
import xyz.block.microfilm.scanning.ImageGroupFixtures.MICROFILM_PNG
import xyz.block.microfilm.scanning.ImageGroupFixtures.RELATIVE_PNG
import xyz.block.microfilm.scanning.ImageGroupFixtures.RELATIVE_WEBP
import xyz.block.microfilm.scanning.ImageGroupFixtures.RESOURCES_DIRECTORY
import xyz.block.microfilm.scanning.ImageGroupFixtures.RESOURCES_PNG
import xyz.block.microfilm.scanning.ImageGroupFixtures.RESOURCES_WEBP

class VerifierTest {
  @Test
  fun `verify matches resource png`() {
    val imageGroup = EMPTY_IMAGE_GROUP.copy(resourcesPng = RESOURCES_PNG)

    assertVerify(
      imageGroup = imageGroup,
      imageSettings = LOSSY_COMPRESS,
      expected = LOSSY_COMPRESS,
    )

    assertVerify(
      imageGroup = imageGroup,
      imageSettings = Exclude,
      expected = Exclude,
    )

    assertVerify(
      imageGroup = imageGroup,
      imageSettings = null,
      expected = Unspecified,
    )
  }

  @Test
  fun `verify matches resource webp`() {
    val imageGroup = EMPTY_IMAGE_GROUP.copy(resourcesWebp = RESOURCES_WEBP)

    assertVerify(
      imageGroup = imageGroup,
      imageSettings = LOSSY_COMPRESS,
      expected = Unspecified,
    )

    assertVerify(
      imageGroup = imageGroup,
      imageSettings = Exclude,
      expected = Exclude,
    )

    assertVerify(
      imageGroup = imageGroup,
      imageSettings = null,
      expected = Unspecified,
    )
  }

  @Test
  fun `verify matches microfilm png`() {
    val imageGroup = EMPTY_IMAGE_GROUP.copy(microfilmPng = MICROFILM_PNG)

    assertVerify(
      imageGroup = imageGroup,
      imageSettings = LOSSY_COMPRESS,
      expected = LOSSY_COMPRESS,
    )

    assertVerify(
      imageGroup = imageGroup,
      imageSettings = Exclude,
      expected = Exclude,
    )

    assertVerify(
      imageGroup = imageGroup,
      imageSettings = null,
      expected = Unspecified,
    )
  }

  @Test
  fun `verify matches microfilm manifest entry`() {
    val imageGroup = EMPTY_IMAGE_GROUP.copy(microfilmManifestEntry = LOSSY_MANIFEST_ENTRY)

    assertVerify(
      imageGroup = imageGroup,
      imageSettings = LOSSY_COMPRESS,
      expected = LOSSY_COMPRESS,
    )

    assertVerify(
      imageGroup = imageGroup,
      imageSettings = Exclude,
      expected = Exclude,
    )

    assertVerify(
      imageGroup = imageGroup,
      imageSettings = null,
      expected = Unspecified,
    )
  }

  @Test
  fun `verify respects image patterns`() {
    assertVerify(
      imageGroup = LOSSY_IMAGE_GROUP,
      imagePattern = RELATIVE_PNG,
      imageSettings = LOSSY_COMPRESS,
      expected = LOSSY_COMPRESS,
    )

    assertVerify(
      imageGroup = LOSSY_IMAGE_GROUP,
      imagePattern = "other.png",
      imageSettings = LOSSY_COMPRESS,
      expected = null,
    )

    assertVerify(
      imageGroup = LOSSY_IMAGE_GROUP,
      imagePattern = RELATIVE_WEBP,
      imageSettings = LOSSY_COMPRESS,
      expected = LOSSY_COMPRESS,
    )

    assertVerify(
      imageGroup = LOSSY_IMAGE_GROUP,
      imagePattern = "other.webp",
      imageSettings = LOSSY_COMPRESS,
      expected = null,
    )
  }

  private fun assertVerify(
    imageGroup: ImageGroup,
    imagePattern: String = "**",
    imageSettings: ImageSettings?,
    expected: ImageSettings?,
  ) {
    val verifier = FakeVerifier<ImageSettings>()
    val scanner = FakeScanner().apply { scanResponses.add(listOf(imageGroup)) }

    verifier.verify(
      scanner = scanner,
      imagePatterns = listOf(imagePattern),
      imageRules =
        if (imageSettings != null) {
          listOf(ImageRule(pattern = "**", imageSettings = imageSettings))
        } else {
          emptyList()
        },
      resourcesDirectory = RESOURCES_DIRECTORY,
      microfilmDirectory = MICROFILM_DIRECTORY,
    )

    if (expected == null) {
      assertThat(verifier.verifyRequests).isEmpty()
    } else {
      assertThat(verifier.verifyRequests).containsExactly(imageGroup to expected)
    }
  }
}
