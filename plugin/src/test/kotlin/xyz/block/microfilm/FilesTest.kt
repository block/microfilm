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
import java.io.File
import org.junit.jupiter.api.Test

class FilesTest {
  @Test
  fun `isPngDrawable for webp drawable`() {
    assertThat(File("src/main/drawable/image.webp").isPngDrawable).isFalse()
  }

  @Test
  fun `isPngDrawable for png asset`() {
    assertThat(File("src/main/asset/image.png").isPngDrawable).isFalse()
  }

  @Test
  fun `isPngDrawable for png drawables`() {
    assertThat(File("src/main/drawable/image.png").isPngDrawable).isTrue()
    assertThat(File("src/main/drawable-hdpi/image.png").isPngDrawable).isTrue()
  }

  @Test
  fun `isPngDrawable for png nine-patch`() {
    assertThat(File("src/main/drawable/image.9.png").isPngDrawable).isFalse()
  }
}
