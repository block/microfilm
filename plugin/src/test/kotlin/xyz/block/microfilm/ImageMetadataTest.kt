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
package xyz.block.microfilm

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class ImageMetadataTest {
  @Test
  fun `toString produces expected options`() {
    assertThat(ImageMetadata(exif = false, icc = false, xmp = false).toString()).isEqualTo("none")
    assertThat(ImageMetadata(exif = false, icc = false, xmp = true).toString()).isEqualTo("xmp")
    assertThat(ImageMetadata(exif = false, icc = true, xmp = false).toString()).isEqualTo("icc")
    assertThat(ImageMetadata(exif = false, icc = true, xmp = true).toString()).isEqualTo("icc,xmp")
    assertThat(ImageMetadata(exif = true, icc = false, xmp = false).toString()).isEqualTo("exif")
    assertThat(ImageMetadata(exif = true, icc = false, xmp = true).toString()).isEqualTo("exif,xmp")
    assertThat(ImageMetadata(exif = true, icc = true, xmp = false).toString()).isEqualTo("exif,icc")
    assertThat(ImageMetadata(exif = true, icc = true, xmp = true).toString()).isEqualTo("all")
  }
}
