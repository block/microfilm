package xyz.block.microfilm

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test

class CompressionRuleTest {
  @Test
  fun `matches path exactly`() {
    val compressionRule = CompressionRule(pattern = "drawable-mdpi/photo.png")

    assertThat(compressionRule.matches(imagePath = "drawable-mdpi/photo.png")).isTrue()
    assertThat(compressionRule.matches(imagePath = "drawable-hdpi/photo_1.png")).isFalse()
    assertThat(compressionRule.matches(imagePath = "drawable-hdpi/photo_2.png")).isFalse()
  }

  @Test
  fun `matches path with wildcards`() {
    val compressionRule = CompressionRule(pattern = "**/photo_*.png")

    assertThat(compressionRule.matches(imagePath = "drawable-mdpi/photo.png")).isFalse()
    assertThat(compressionRule.matches(imagePath = "drawable-hdpi/photo_1.png")).isTrue()
    assertThat(compressionRule.matches(imagePath = "drawable-hdpi/photo_2.png")).isTrue()
  }

  @Test
  fun `matches fallback`() {
    val compressionRule = CompressionRule(pattern = "**")

    assertThat(compressionRule.matches(imagePath = "drawable-mdpi/photo.png")).isTrue()
    assertThat(compressionRule.matches(imagePath = "drawable-hdpi/photo_1.png")).isTrue()
    assertThat(compressionRule.matches(imagePath = "drawable-hdpi/photo_2.png")).isTrue()
  }

  @Test
  fun `resolve override`() {
    val fallback = CompressionRule(pattern = "**")
    val mdpi = CompressionRule(pattern = "drawable-mdpi/**")
    val hdpi = CompressionRule(pattern = "drawable-hdpi/**")
    val compressionRules = listOf(fallback, mdpi, hdpi)

    assertThat(compressionRules.resolve(imagePath = "drawable-mdpi/photo.png")).isEqualTo(mdpi)
  }

  @Test
  fun `resolve fallback`() {
    val fallback = CompressionRule(pattern = "**")
    val mdpi = CompressionRule(pattern = "drawable-mdpi/**")
    val hdpi = CompressionRule(pattern = "drawable-hdpi/**")
    val compressionRules = listOf(fallback, mdpi, hdpi)

    assertThat(compressionRules.resolve(imagePath = "drawable-xhdpi/photo.png")).isEqualTo(fallback)
  }

  private fun CompressionRule(pattern: String) =
    CompressionRule(
      pattern = pattern,
      compressionSettings = CompressionSettings(lossless = true, quality = null),
    )
}
