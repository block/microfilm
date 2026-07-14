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
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import xyz.block.microfilm.ImageGroupFixtures.LOSSY_COMPRESS
import xyz.block.microfilm.ImageGroupFixtures.LOSSY_IMAGE_GROUP
import xyz.block.microfilm.ImageSettings.Compress
import xyz.block.microfilm.ImageSettings.Exclude
import xyz.block.microfilm.ImageSettings.Unspecified

class TypedCompressorTest {
  private val compressCompressor = FakeCompressor<Compress>()
  private val excludeCompressor = FakeCompressor<Exclude>()
  private val unspecifiedCompressor = FakeCompressor<Unspecified>()

  private val compressor =
    TypedCompressor(
      compressCompressor = compressCompressor,
      excludeCompressor = excludeCompressor,
      unspecifiedCompressor = unspecifiedCompressor,
    )

  @Test
  fun `compress delegates to compress compressor`() {
    compressor.compress(imageGroup = LOSSY_IMAGE_GROUP, imageSettings = LOSSY_COMPRESS)

    assertThat(compressCompressor.compressRequests.first())
      .isEqualTo(LOSSY_IMAGE_GROUP to LOSSY_COMPRESS)
    assertThat(excludeCompressor.compressRequests).isEmpty()
    assertThat(unspecifiedCompressor.compressRequests).isEmpty()
  }

  @Test
  fun `compress delegates to exclude compressor`() {
    compressor.compress(imageGroup = LOSSY_IMAGE_GROUP, imageSettings = Exclude)

    assertThat(compressCompressor.compressRequests).isEmpty()
    assertThat(excludeCompressor.compressRequests.first()).isEqualTo(LOSSY_IMAGE_GROUP to Exclude)
    assertThat(unspecifiedCompressor.compressRequests).isEmpty()
  }

  @Test
  fun `compress delegates to unspecified compressor`() {
    compressor.compress(imageGroup = LOSSY_IMAGE_GROUP, imageSettings = Unspecified)

    assertThat(compressCompressor.compressRequests).isEmpty()
    assertThat(excludeCompressor.compressRequests).isEmpty()
    assertThat(unspecifiedCompressor.compressRequests.first())
      .isEqualTo(LOSSY_IMAGE_GROUP to Unspecified)
  }
}
