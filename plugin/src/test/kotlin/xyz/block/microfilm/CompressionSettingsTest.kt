package xyz.block.microfilm

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CompressionSettingsTest {
  @Test
  fun `init validates quality range`() {
    assertThrows<IllegalArgumentException> { CompressionSettings(lossless = true, quality = -1) }
    CompressionSettings(lossless = true, quality = 0)
    CompressionSettings(lossless = true, quality = 100)
    assertThrows<IllegalArgumentException> { CompressionSettings(lossless = true, quality = 101) }
  }
}
