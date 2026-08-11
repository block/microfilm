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
package xyz.block.microfilm.decompression

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import org.junit.jupiter.api.Test
import xyz.block.microfilm.scanning.FakeScanner
import xyz.block.microfilm.scanning.ImageGroup
import xyz.block.microfilm.scanning.ImageGroupFixtures.LOSSY_IMAGE_GROUP
import xyz.block.microfilm.scanning.ImageGroupFixtures.MICROFILM_DIRECTORY
import xyz.block.microfilm.scanning.ImageGroupFixtures.RELATIVE_PNG
import xyz.block.microfilm.scanning.ImageGroupFixtures.RELATIVE_WEBP
import xyz.block.microfilm.scanning.ImageGroupFixtures.RESOURCES_DIRECTORY

class DecompressorTest {
  @Test
  fun `decompress respects image patterns`() {
    assertDecompress(
      imageGroup = LOSSY_IMAGE_GROUP,
      imagePattern = RELATIVE_PNG,
      expected = true,
    )

    assertDecompress(
      imageGroup = LOSSY_IMAGE_GROUP,
      imagePattern = "other.png",
      expected = false,
    )

    assertDecompress(
      imageGroup = LOSSY_IMAGE_GROUP,
      imagePattern = RELATIVE_WEBP,
      expected = true,
    )

    assertDecompress(
      imageGroup = LOSSY_IMAGE_GROUP,
      imagePattern = "other.webp",
      expected = false,
    )
  }

  private fun assertDecompress(
    imageGroup: ImageGroup,
    imagePattern: String = "**",
    expected: Boolean,
  ) {
    val decompressor = FakeDecompressor()
    val scanner = FakeScanner().apply { scanResponses.add(listOf(imageGroup)) }

    val manifestEntries =
      decompressor.decompress(
        scanner = scanner,
        imagePatterns = listOf(imagePattern),
        resourcesDirectory = RESOURCES_DIRECTORY,
        microfilmDirectory = MICROFILM_DIRECTORY,
      )

    if (expected) {
      assertThat(decompressor.decompressRequests).containsExactly(imageGroup)
      assertThat(manifestEntries).isEmpty()
    } else {
      assertThat(decompressor.decompressRequests).isEmpty()
      assertThat(manifestEntries).containsExactly(imageGroup.microfilmManifestEntry)
    }
  }
}
