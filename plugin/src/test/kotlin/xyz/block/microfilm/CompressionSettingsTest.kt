package xyz.block.microfilm

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CompressionSettingsTest {
  @Test
  fun `init validates compressionFactor range`() {
    assertThrows<IllegalArgumentException> {
      CompressionSettings(lossless = true, compressionFactor = -1)
    }

    CompressionSettings(lossless = true, compressionFactor = null)
    CompressionSettings(lossless = true, compressionFactor = 0)
    CompressionSettings(lossless = true, compressionFactor = 100)

    assertThrows<IllegalArgumentException> {
      CompressionSettings(lossless = true, compressionFactor = 101)
    }
  }
}
