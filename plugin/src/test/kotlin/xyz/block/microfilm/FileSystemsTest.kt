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
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import okio.ByteString.Companion.encodeUtf8
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import org.junit.jupiter.api.Test
import xyz.block.microfilm.ImageGroupFixtures.PNG_CONTENT
import xyz.block.microfilm.ImageGroupFixtures.WEBP_CONTENT

class FileSystemsTest {
  val fileSystem =
    FakeFileSystem().apply {
      createDirectories(dir = DRAWABLE_DIRECTORY)
      write(file = PNG_IMAGE) { write(PNG_CONTENT.encodeUtf8()) }
      write(file = WEBP_IMAGE) { write(WEBP_CONTENT.encodeUtf8()) }
    }

  @Test
  fun `listRecursivelyOrEmpty returns files for directory that exists`() {
    assertThat(fileSystem.listRecursivelyOrEmpty(dir = RES_DIRECTORY))
      .containsExactly(DRAWABLE_DIRECTORY, PNG_IMAGE, WEBP_IMAGE)
  }

  @Test
  fun `listRecursivelyOrEmpty returns nothing for directory that does not exist`() {
    assertThat(fileSystem.listRecursivelyOrEmpty(dir = "src/main/other".toPath())).isEmpty()
  }

  @Test
  fun `listRecursivelyOrEmpty returns nothing for regular file`() {
    assertThat(fileSystem.listRecursivelyOrEmpty(dir = PNG_IMAGE)).isEmpty()
  }

  companion object {
    private val RES_DIRECTORY = "src/main/res".toPath()
    private val DRAWABLE_DIRECTORY = RES_DIRECTORY.resolve(child = "drawable")
    private val PNG_IMAGE = DRAWABLE_DIRECTORY.resolve(child = "image.png")
    private val WEBP_IMAGE = DRAWABLE_DIRECTORY.resolve(child = "image.webp")
  }
}
