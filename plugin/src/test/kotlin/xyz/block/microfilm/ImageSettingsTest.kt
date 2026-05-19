package xyz.block.microfilm

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ImageSettingsTest {
  @Test
  fun `init validates compressionFactor range`() {
    assertThrows<IllegalArgumentException> {
      ImageSettings(lossless = true, compressionFactor = -1)
    }

    ImageSettings(lossless = true, compressionFactor = null)
    ImageSettings(lossless = true, compressionFactor = 0)
    ImageSettings(lossless = true, compressionFactor = 100)

    assertThrows<IllegalArgumentException> {
      ImageSettings(lossless = true, compressionFactor = 101)
    }
  }
}
