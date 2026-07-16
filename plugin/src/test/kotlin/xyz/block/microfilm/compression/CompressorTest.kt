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

import assertk.assertThat
import assertk.assertions.containsExactly
import org.junit.jupiter.api.Test
import xyz.block.microfilm.ImageGroupFixtures.EMPTY_IMAGE_GROUP
import xyz.block.microfilm.ImageGroupFixtures.LOSSY_COMPRESS
import xyz.block.microfilm.ImageGroupFixtures.LOSSY_MANIFEST_ENTRY
import xyz.block.microfilm.ImageGroupFixtures.MICROFILM_DIRECTORY
import xyz.block.microfilm.ImageGroupFixtures.MICROFILM_PNG
import xyz.block.microfilm.ImageGroupFixtures.RESOURCES_DIRECTORY
import xyz.block.microfilm.ImageGroupFixtures.RESOURCES_PNG
import xyz.block.microfilm.ImageGroupFixtures.RESOURCES_WEBP
import xyz.block.microfilm.ImageRule
import xyz.block.microfilm.ImageSettings
import xyz.block.microfilm.ImageSettings.Exclude
import xyz.block.microfilm.ImageSettings.Unspecified
import xyz.block.microfilm.scanning.FakeScanner
import xyz.block.microfilm.scanning.ImageGroup

class CompressorTest {
  @Test
  fun `compress matches resource png`() {
    val imageGroup = EMPTY_IMAGE_GROUP.copy(resourcesPng = RESOURCES_PNG)

    verifyCompress(
      imageGroup = imageGroup,
      imageSettings = LOSSY_COMPRESS,
      expected = LOSSY_COMPRESS,
    )

    verifyCompress(
      imageGroup = imageGroup,
      imageSettings = Exclude,
      expected = Exclude,
    )

    verifyCompress(
      imageGroup = imageGroup,
      imageSettings = null,
      expected = Unspecified,
    )
  }

  @Test
  fun `compress matches resource webp`() {
    val imageGroup = EMPTY_IMAGE_GROUP.copy(resourcesWebp = RESOURCES_WEBP)

    verifyCompress(
      imageGroup = imageGroup,
      imageSettings = LOSSY_COMPRESS,
      expected = Unspecified,
    )

    verifyCompress(
      imageGroup = imageGroup,
      imageSettings = Exclude,
      expected = Exclude,
    )

    verifyCompress(
      imageGroup = imageGroup,
      imageSettings = null,
      expected = Unspecified,
    )
  }

  @Test
  fun `compress matches microfilm png`() {
    val imageGroup = EMPTY_IMAGE_GROUP.copy(microfilmPng = MICROFILM_PNG)

    verifyCompress(
      imageGroup = imageGroup,
      imageSettings = LOSSY_COMPRESS,
      expected = LOSSY_COMPRESS,
    )

    verifyCompress(
      imageGroup = imageGroup,
      imageSettings = Exclude,
      expected = Exclude,
    )

    verifyCompress(
      imageGroup = imageGroup,
      imageSettings = null,
      expected = Unspecified,
    )
  }

  @Test
  fun `compress matches microfilm manifest entry`() {
    val imageGroup = EMPTY_IMAGE_GROUP.copy(microfilmManifestEntry = LOSSY_MANIFEST_ENTRY)

    verifyCompress(
      imageGroup = imageGroup,
      imageSettings = LOSSY_COMPRESS,
      expected = LOSSY_COMPRESS,
    )

    verifyCompress(
      imageGroup = imageGroup,
      imageSettings = Exclude,
      expected = Exclude,
    )

    verifyCompress(
      imageGroup = imageGroup,
      imageSettings = null,
      expected = Unspecified,
    )
  }

  private fun verifyCompress(
    imageGroup: ImageGroup,
    imageSettings: ImageSettings?,
    expected: ImageSettings,
  ) {
    val compressor = FakeCompressor<ImageSettings>()
    val scanner = FakeScanner().apply { scanResponses.add(listOf(imageGroup)) }

    compressor.compress(
      scanner = scanner,
      imageRules =
        if (imageSettings != null) {
          listOf(ImageRule(pattern = "**", imageSettings = imageSettings))
        } else {
          emptyList()
        },
      resourcesDirectory = RESOURCES_DIRECTORY,
      microfilmDirectory = MICROFILM_DIRECTORY,
    )

    assertThat(compressor.compressRequests).containsExactly(imageGroup to expected)
  }
}
