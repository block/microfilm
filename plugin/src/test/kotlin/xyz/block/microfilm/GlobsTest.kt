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
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import okio.Path.Companion.toPath
import org.junit.jupiter.api.Test

class GlobsTest {
  @Test
  fun `single wildcard stays within one directory`() {
    assertThat(PNG.matchesGlob(pattern = "*/photo.png")).isTrue()
    assertThat(NESTED_PNG.matchesGlob(pattern = "*/photo.png")).isFalse()
  }

  @Test
  fun `double wildcard crosses directories`() {
    assertThat(PNG.matchesGlob(pattern = "**/photo.png")).isTrue()
    assertThat(NESTED_PNG.matchesGlob(pattern = "**/photo.png")).isTrue()
  }

  @Test
  fun `double wildcard requires a directory`() {
    assertThat("photo.png".matchesGlob(pattern = "**/photo.png")).isFalse()
    assertThat("photo.png".matchesGlob(pattern = "**")).isTrue()
  }

  @Test
  fun `alternation matches either extension`() {
    assertThat(PNG.matchesGlob(pattern = "**/photo.{png,webp}")).isTrue()
    assertThat(WEBP.matchesGlob(pattern = "**/photo.{png,webp}")).isTrue()
    assertThat(OTHER_PNG.matchesGlob(pattern = "**/photo.{png,webp}")).isFalse()
  }

  @Test
  fun `path matches the same patterns as its string`() {
    assertThat(PNG.toPath().matchesGlob(pattern = "**/photo.png")).isTrue()
    assertThat(OTHER_PNG.toPath().matchesGlob(pattern = "**/photo.png")).isFalse()
  }

  private companion object {
    private const val PNG = "drawable-mdpi/photo.png"
    private const val WEBP = "drawable-mdpi/photo.webp"
    private const val NESTED_PNG = "drawable-mdpi/nested/photo.png"
    private const val OTHER_PNG = "drawable-mdpi/other.png"
  }
}
