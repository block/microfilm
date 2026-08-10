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
package xyz.block.microfilm.decompression

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import okio.ByteString.Companion.encodeUtf8
import okio.fakefilesystem.FakeFileSystem
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import xyz.block.microfilm.scanning.ImageGroupFixtures.EMPTY_IMAGE_GROUP
import xyz.block.microfilm.scanning.ImageGroupFixtures.LOSSY_IMAGE_GROUP
import xyz.block.microfilm.scanning.ImageGroupFixtures.LOSSY_MANIFEST_ENTRY
import xyz.block.microfilm.scanning.ImageGroupFixtures.MICROFILM_DIRECTORY
import xyz.block.microfilm.scanning.ImageGroupFixtures.MICROFILM_PNG
import xyz.block.microfilm.scanning.ImageGroupFixtures.PNG_CONTENT
import xyz.block.microfilm.scanning.ImageGroupFixtures.RESOURCES_DIRECTORY
import xyz.block.microfilm.scanning.ImageGroupFixtures.RESOURCES_PNG
import xyz.block.microfilm.scanning.ImageGroupFixtures.RESOURCES_WEBP
import xyz.block.microfilm.scanning.ImageGroupFixtures.WEBP_CONTENT

class RealDecompressorTest {
  private val fileSystem = FakeFileSystem()

  private val decompressor =
    RealDecompressor(
      fileSystem = fileSystem,
      resourcesDirectory = RESOURCES_DIRECTORY,
      microfilmDirectory = MICROFILM_DIRECTORY,
    )

  @AfterEach
  fun tearDown() {
    fileSystem.checkNoOpenFiles()
  }

  @Test
  fun `decompress ignores lone resources png`() {
    fileSystem.createDirectories(dir = RESOURCES_PNG.parent!!)
    fileSystem.write(file = RESOURCES_PNG) { write(byteString = PNG_CONTENT.encodeUtf8()) }

    decompressor.decompress(imageGroup = EMPTY_IMAGE_GROUP.copy(resourcesPng = RESOURCES_PNG))

    assertThat(fileSystem.exists(RESOURCES_PNG)).isTrue()
    assertThat(fileSystem.exists(RESOURCES_WEBP)).isFalse()
    assertThat(fileSystem.exists(MICROFILM_PNG)).isFalse()
  }

  @Test
  fun `decompress ignores lone resources webp`() {
    fileSystem.createDirectories(dir = RESOURCES_WEBP.parent!!)
    fileSystem.write(file = RESOURCES_WEBP) { write(byteString = WEBP_CONTENT.encodeUtf8()) }

    decompressor.decompress(imageGroup = EMPTY_IMAGE_GROUP.copy(resourcesWebp = RESOURCES_WEBP))

    assertThat(fileSystem.exists(RESOURCES_PNG)).isFalse()
    assertThat(fileSystem.exists(RESOURCES_WEBP)).isTrue()
    assertThat(fileSystem.exists(MICROFILM_PNG)).isFalse()
  }

  @Test
  fun `decompress restores microfilm png`() {
    fileSystem.createDirectories(dir = MICROFILM_PNG.parent!!)
    fileSystem.write(file = MICROFILM_PNG) { write(byteString = PNG_CONTENT.encodeUtf8()) }

    decompressor.decompress(imageGroup = EMPTY_IMAGE_GROUP.copy(microfilmPng = MICROFILM_PNG))

    assertThat(fileSystem.exists(RESOURCES_PNG)).isTrue()
    assertThat(fileSystem.exists(RESOURCES_WEBP)).isFalse()
    assertThat(fileSystem.exists(MICROFILM_PNG)).isFalse()
  }

  @Test
  fun `decompress restores microfilm png and removes resources webp`() {
    fileSystem.createDirectories(dir = RESOURCES_WEBP.parent!!)
    fileSystem.createDirectories(dir = MICROFILM_PNG.parent!!)
    fileSystem.write(file = RESOURCES_WEBP) { write(byteString = WEBP_CONTENT.encodeUtf8()) }
    fileSystem.write(file = MICROFILM_PNG) { write(byteString = PNG_CONTENT.encodeUtf8()) }

    decompressor.decompress(imageGroup = LOSSY_IMAGE_GROUP)

    assertThat(fileSystem.exists(RESOURCES_PNG)).isTrue()
    assertThat(fileSystem.exists(RESOURCES_WEBP)).isFalse()
    assertThat(fileSystem.exists(MICROFILM_PNG)).isFalse()
  }

  @Test
  fun `decompress ignores lone manifest entry microfilm png`() {
    decompressor.decompress(
      imageGroup = EMPTY_IMAGE_GROUP.copy(microfilmManifestEntry = LOSSY_MANIFEST_ENTRY)
    )

    assertThat(fileSystem.exists(RESOURCES_PNG)).isFalse()
    assertThat(fileSystem.exists(RESOURCES_WEBP)).isFalse()
    assertThat(fileSystem.exists(MICROFILM_PNG)).isFalse()
  }
}
