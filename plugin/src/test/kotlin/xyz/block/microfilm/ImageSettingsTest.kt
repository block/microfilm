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

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import xyz.block.microfilm.ImageSettings.Compress

class ImageSettingsTest {
  @Test
  fun `compress init validates compressionFactor range`() {
    assertThrows<IllegalArgumentException> { Compress(compressionFactor = -1) }

    Compress(compressionFactor = null)
    Compress(compressionFactor = 0)
    Compress(compressionFactor = 100)

    assertThrows<IllegalArgumentException> { Compress(compressionFactor = 101) }
  }

  @Test
  fun `compress init validates compressionMethod range`() {
    assertThrows<IllegalArgumentException> { Compress(compressionMethod = -1) }

    Compress(compressionMethod = null)
    Compress(compressionMethod = 0)
    Compress(compressionMethod = 6)

    assertThrows<IllegalArgumentException> { Compress(compressionMethod = 7) }
  }
}
