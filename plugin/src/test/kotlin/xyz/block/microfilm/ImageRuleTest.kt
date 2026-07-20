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
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import okio.Path.Companion.toPath
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

    val imagePath = "drawable-mdpi/photo.png"
    assertThat(imageRules.resolve(imagePath = imagePath)).isEqualTo(mdpi)
    assertThat(imageRules.resolve(imagePath = imagePath.toPath())).isEqualTo(mdpi)
  }

  @Test
  fun `resolve fallback`() {
    val fallback = ImageRule(pattern = "**")
    val mdpi = ImageRule(pattern = "drawable-mdpi/**")
    val hdpi = ImageRule(pattern = "drawable-hdpi/**")
    val imageRules = listOf(fallback, mdpi, hdpi)

    val imagePath = "drawable-xhdpi/photo.png"
    assertThat(imageRules.resolve(imagePath = imagePath)).isEqualTo(fallback)
    assertThat(imageRules.resolve(imagePath = imagePath.toPath())).isEqualTo(fallback)
  }

  @Test
  fun `resolve nothing`() {
    val mdpi = ImageRule(pattern = "drawable-mdpi/**")
    val hdpi = ImageRule(pattern = "drawable-hdpi/**")
    val imageRules = listOf(mdpi, hdpi)

    val imagePath = "drawable-xhdpi/photo.png"
    assertThat(imageRules.resolve(imagePath = imagePath)).isNull()
    assertThat(imageRules.resolve(imagePath = imagePath.toPath())).isNull()
  }

  private fun ImageRule(pattern: String) =
    ImageRule(
      pattern = pattern,
      imageSettings = ImageSettings.Compress(lossless = true, compressionFactor = null),
    )
}
