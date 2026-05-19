package xyz.block.microfilm

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test

class ImageRuleTest {
  @Test
  fun `matches path exactly`() {
    val imageRule = ImageRule(pattern = "drawable-mdpi/photo.png")

    assertThat(imageRule.matches(imagePath = "drawable-mdpi/photo.png")).isTrue()
    assertThat(imageRule.matches(imagePath = "drawable-hdpi/photo_1.png")).isFalse()
    assertThat(imageRule.matches(imagePath = "drawable-hdpi/photo_2.png")).isFalse()
  }

  @Test
  fun `matches path with wildcards`() {
    val imageRule = ImageRule(pattern = "**/photo_*.png")

    assertThat(imageRule.matches(imagePath = "drawable-mdpi/photo.png")).isFalse()
    assertThat(imageRule.matches(imagePath = "drawable-hdpi/photo_1.png")).isTrue()
    assertThat(imageRule.matches(imagePath = "drawable-hdpi/photo_2.png")).isTrue()
  }

  @Test
  fun `matches fallback`() {
    val imageRule = ImageRule(pattern = "**")

    assertThat(imageRule.matches(imagePath = "drawable-mdpi/photo.png")).isTrue()
    assertThat(imageRule.matches(imagePath = "drawable-hdpi/photo_1.png")).isTrue()
    assertThat(imageRule.matches(imagePath = "drawable-hdpi/photo_2.png")).isTrue()
  }

  @Test
  fun `resolve override`() {
    val fallback = ImageRule(pattern = "**")
    val mdpi = ImageRule(pattern = "drawable-mdpi/**")
    val hdpi = ImageRule(pattern = "drawable-hdpi/**")
    val imageRules = listOf(fallback, mdpi, hdpi)

    assertThat(imageRules.resolve(imagePath = "drawable-mdpi/photo.png")).isEqualTo(mdpi)
  }

  @Test
  fun `resolve fallback`() {
    val fallback = ImageRule(pattern = "**")
    val mdpi = ImageRule(pattern = "drawable-mdpi/**")
    val hdpi = ImageRule(pattern = "drawable-hdpi/**")
    val imageRules = listOf(fallback, mdpi, hdpi)

    assertThat(imageRules.resolve(imagePath = "drawable-xhdpi/photo.png")).isEqualTo(fallback)
  }

  @Test
  fun `resolve nothing`() {
    val mdpi = ImageRule(pattern = "drawable-mdpi/**")
    val hdpi = ImageRule(pattern = "drawable-hdpi/**")
    val imageRules = listOf(mdpi, hdpi)

    assertThat(imageRules.resolve(imagePath = "drawable-xhdpi/photo.png")).isNull()
  }

  private fun ImageRule(pattern: String) =
    ImageRule(
      pattern = pattern,
      imageSettings = ImageSettings(lossless = true, compressionFactor = null),
    )
}
