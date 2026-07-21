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
package xyz.block.microfilm.compression

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import okio.ByteString.Companion.encodeUtf8
import okio.fakefilesystem.FakeFileSystem
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import xyz.block.microfilm.ImageSettings.Exclude
import xyz.block.microfilm.compression.Compressor.Result.Success
import xyz.block.microfilm.scanning.ImageGroupFixtures.EMPTY_IMAGE_GROUP
import xyz.block.microfilm.scanning.ImageGroupFixtures.LOSSY_MANIFEST_ENTRY
import xyz.block.microfilm.scanning.ImageGroupFixtures.MICROFILM_DIRECTORY
import xyz.block.microfilm.scanning.ImageGroupFixtures.MICROFILM_PNG
import xyz.block.microfilm.scanning.ImageGroupFixtures.PNG_CONTENT
import xyz.block.microfilm.scanning.ImageGroupFixtures.RESOURCES_DIRECTORY
import xyz.block.microfilm.scanning.ImageGroupFixtures.RESOURCES_PNG
import xyz.block.microfilm.scanning.ImageGroupFixtures.RESOURCES_WEBP

class ExcludeCompressorTest {
  private val fileSystem = FakeFileSystem()

  private val compressor =
    ExcludeCompressor(
      fileSystem = fileSystem,
      resourcesDirectory = RESOURCES_DIRECTORY,
      microfilmDirectory = MICROFILM_DIRECTORY,
    )

  @AfterEach
  fun tearDown() {
    fileSystem.checkNoOpenFiles()
  }

  @Test
  fun `compress succeeds with resources png`() {
    val result =
      compressor.compress(
        imageGroup = EMPTY_IMAGE_GROUP.copy(resourcesPng = RESOURCES_PNG),
        imageSettings = Exclude,
      )

    assertThat(result).isEqualTo(Success(microfilmManifestEntry = null))
  }

  @Test
  fun `compress succeeds with resources webp`() {
    val result =
      compressor.compress(
        imageGroup = EMPTY_IMAGE_GROUP.copy(resourcesWebp = RESOURCES_WEBP),
        imageSettings = Exclude,
      )

    assertThat(result).isEqualTo(Success(microfilmManifestEntry = null))
  }

  @Test
  fun `compress succeeds with microfilm png after moving it back to resources`() {
    fileSystem.createDirectories(dir = MICROFILM_PNG.parent!!)
    fileSystem.write(file = MICROFILM_PNG) { write(byteString = PNG_CONTENT.encodeUtf8()) }

    val result =
      compressor.compress(
        imageGroup = EMPTY_IMAGE_GROUP.copy(microfilmPng = MICROFILM_PNG),
        imageSettings = Exclude,
      )

    assertThat(fileSystem.exists(MICROFILM_PNG)).isFalse()
    assertThat(fileSystem.read(file = RESOURCES_PNG) { readUtf8() }).isEqualTo(PNG_CONTENT)
    assertThat(result).isEqualTo(Success(microfilmManifestEntry = null))
  }

  @Test
  fun `compress succeeds with microfilm png after deleting it`() {
    fileSystem.createDirectories(dir = RESOURCES_PNG.parent!!)
    fileSystem.createDirectories(dir = MICROFILM_PNG.parent!!)
    fileSystem.write(file = RESOURCES_PNG) { write(byteString = PNG_CONTENT.encodeUtf8()) }
    fileSystem.write(file = MICROFILM_PNG) { write(byteString = "other".encodeUtf8()) }

    val result =
      compressor.compress(
        imageGroup =
          EMPTY_IMAGE_GROUP.copy(resourcesPng = RESOURCES_PNG, microfilmPng = MICROFILM_PNG),
        imageSettings = Exclude,
      )

    assertThat(fileSystem.exists(MICROFILM_PNG)).isFalse()
    assertThat(fileSystem.read(file = RESOURCES_PNG) { readUtf8() }).isEqualTo(PNG_CONTENT)
    assertThat(result).isEqualTo(Success(microfilmManifestEntry = null))
  }

  @Test
  fun `compress succeeds with microfilm manifest entry`() {
    val result =
      compressor.compress(
        imageGroup = EMPTY_IMAGE_GROUP.copy(microfilmManifestEntry = LOSSY_MANIFEST_ENTRY),
        imageSettings = Exclude,
      )

    assertThat(result).isEqualTo(Success(microfilmManifestEntry = null))
  }
}
