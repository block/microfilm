package xyz.block.microfilm

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ImageSettingsTest {
  @Test
  fun `compress init validates compressionFactor range`() {
    assertThrows<IllegalArgumentException> {
      ImageSettings.Compress(lossless = true, compressionFactor = -1)
    }

    ImageSettings.Compress(lossless = true, compressionFactor = null)
    ImageSettings.Compress(lossless = true, compressionFactor = 0)
    ImageSettings.Compress(lossless = true, compressionFactor = 100)

    assertThrows<IllegalArgumentException> {
      ImageSettings.Compress(lossless = true, compressionFactor = 101)
    }
  }
}
