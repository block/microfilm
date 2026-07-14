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

class PathsTest {
  @Test
  fun `isInDrawableDirectory is true for file in unqualified drawable directory`() {
    assertThat("src/main/res/drawable/image.png".toPath().isInDrawableDirectory).isTrue()
  }

  @Test
  fun `isInDrawableDirectory is true for file in qualified drawable directories`() {
    assertThat("src/main/res/drawable-anydpi/image.png".toPath().isInDrawableDirectory).isTrue()
    assertThat("src/main/res/drawable-nodpi/image.png".toPath().isInDrawableDirectory).isTrue()
    assertThat("src/main/res/drawable-ldpi/image.png".toPath().isInDrawableDirectory).isTrue()
    assertThat("src/main/res/drawable-mdpi/image.png".toPath().isInDrawableDirectory).isTrue()
    assertThat("src/main/res/drawable-hdpi/image.png".toPath().isInDrawableDirectory).isTrue()
    assertThat("src/main/res/drawable-xhdpi/image.png".toPath().isInDrawableDirectory).isTrue()
    assertThat("src/main/res/drawable-xxhdpi/image.png".toPath().isInDrawableDirectory).isTrue()
    assertThat("src/main/res/drawable-xxxhdpi/image.png".toPath().isInDrawableDirectory).isTrue()
    assertThat("src/main/res/drawable-night/image.png".toPath().isInDrawableDirectory).isTrue()
    assertThat("src/main/res/drawable-v24/image.png".toPath().isInDrawableDirectory).isTrue()
    assertThat("src/main/res/drawable-land/image.png".toPath().isInDrawableDirectory).isTrue()
  }

  @Test
  fun `isInDrawableDirectory is false for file in other directory`() {
    assertThat("src/main/res/assets/image.png".toPath().isInDrawableDirectory).isFalse()
  }

  @Test
  fun `isPngDrawable is true for png in drawable directory`() {
    assertThat("src/main/res/drawable/image.png".toPath().isPngDrawable).isTrue()
    assertThat("src/main/res/drawable/IMAGE.PNG".toPath().isPngDrawable).isTrue()
  }

  @Test
  fun `isPngDrawable is false for png in other directory`() {
    assertThat("src/main/res/assets/image.png".toPath().isPngDrawable).isFalse()
  }

  @Test
  fun `isPngDrawable is false for nine-patch in drawable directory`() {
    assertThat("src/main/res/drawable/image.9.png".toPath().isPngDrawable).isFalse()
  }

  @Test
  fun `isPngDrawable is false for webp in drawable directory`() {
    assertThat("src/main/res/drawable/image.webp".toPath().isPngDrawable).isFalse()
  }

  @Test
  fun `isPngDrawable is false for directory in drawable directory`() {
    assertThat("src/main/res/drawable/nested".toPath().isPngDrawable).isFalse()
  }

  @Test
  fun `isWebpDrawable is true for webp in drawable directory`() {
    assertThat("src/main/res/drawable/image.webp".toPath().isWebpDrawable).isTrue()
    assertThat("src/main/res/drawable/IMAGE.WEBP".toPath().isWebpDrawable).isTrue()
  }

  @Test
  fun `isWebpDrawable is false for webp in other directory`() {
    assertThat("src/main/res/assets/image.webp".toPath().isWebpDrawable).isFalse()
  }

  @Test
  fun `isWebpDrawable is false for png in drawable directory`() {
    assertThat("src/main/res/drawable/image.png".toPath().isWebpDrawable).isFalse()
  }

  @Test
  fun `isWebpDrawable is false for directory in drawable directory`() {
    assertThat("src/main/res/drawable/nested".toPath().isWebpDrawable).isFalse()
  }
}
