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
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import okio.ByteString.Companion.encodeUtf8
import okio.fakefilesystem.FakeFileSystem
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import xyz.block.microfilm.compression.Compressor.Result.Success
import xyz.block.microfilm.cwebp.FakeCwebp
import xyz.block.microfilm.scanning.ImageGroupFixtures.EMPTY_IMAGE_GROUP
import xyz.block.microfilm.scanning.ImageGroupFixtures.LOSSLESS_COMPRESS
import xyz.block.microfilm.scanning.ImageGroupFixtures.LOSSLESS_MANIFEST_ENTRY
import xyz.block.microfilm.scanning.ImageGroupFixtures.LOSSY_COMPRESS
import xyz.block.microfilm.scanning.ImageGroupFixtures.LOSSY_IMAGE_GROUP
import xyz.block.microfilm.scanning.ImageGroupFixtures.LOSSY_MANIFEST_ENTRY
import xyz.block.microfilm.scanning.ImageGroupFixtures.MICROFILM_DIRECTORY
import xyz.block.microfilm.scanning.ImageGroupFixtures.MICROFILM_PNG
import xyz.block.microfilm.scanning.ImageGroupFixtures.PNG_CONTENT
import xyz.block.microfilm.scanning.ImageGroupFixtures.RESOURCES_DIRECTORY
import xyz.block.microfilm.scanning.ImageGroupFixtures.RESOURCES_PNG
import xyz.block.microfilm.scanning.ImageGroupFixtures.RESOURCES_WEBP
import xyz.block.microfilm.scanning.ImageGroupFixtures.WEBP_CONTENT

class CompressCompressorTest {
  private val fileSystem =
    FakeFileSystem().apply {
      createDirectories(dir = RESOURCES_PNG.parent!!)
      createDirectories(dir = MICROFILM_PNG.parent!!)
    }
  private val cwebp = FakeCwebp(fileSystem = fileSystem, convertToWebp = { WEBP_CONTENT })

  private val compressor =
    CompressCompressor(
      cwebp = cwebp,
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
    fileSystem.write(file = RESOURCES_PNG) { write(PNG_CONTENT.encodeUtf8()) }

    val result =
      compressor.compress(
        imageGroup = EMPTY_IMAGE_GROUP.copy(resourcesPng = RESOURCES_PNG),
        imageSettings = LOSSY_COMPRESS,
      )

    assertThat(cwebp.compressions).containsExactly(MICROFILM_PNG to RESOURCES_WEBP)
    assertThat(fileSystem.exists(path = RESOURCES_PNG)).isFalse()
    assertThat(fileSystem.exists(path = RESOURCES_WEBP)).isTrue()
    assertThat(fileSystem.exists(path = MICROFILM_PNG)).isTrue()
    assertThat(result).isEqualTo(Success(microfilmManifestEntry = LOSSY_MANIFEST_ENTRY))
  }

  @Test
  fun `compress succeeds with resources webp`() {
    fileSystem.write(file = RESOURCES_WEBP) { write(WEBP_CONTENT.encodeUtf8()) }

    val result =
      compressor.compress(
        imageGroup = EMPTY_IMAGE_GROUP.copy(resourcesWebp = RESOURCES_WEBP),
        imageSettings = LOSSY_COMPRESS,
      )

    assertThat(cwebp.compressions).isEmpty()
    assertThat(fileSystem.exists(path = RESOURCES_PNG)).isFalse()
    assertThat(fileSystem.exists(path = RESOURCES_WEBP)).isFalse()
    assertThat(fileSystem.exists(path = MICROFILM_PNG)).isFalse()
    assertThat(result).isEqualTo(Success(microfilmManifestEntry = null))
  }

  @Test
  fun `compress succeeds with microfilm png`() {
    fileSystem.write(file = MICROFILM_PNG) { write(PNG_CONTENT.encodeUtf8()) }

    val result =
      compressor.compress(
        imageGroup = EMPTY_IMAGE_GROUP.copy(microfilmPng = MICROFILM_PNG),
        imageSettings = LOSSY_COMPRESS,
      )

    assertThat(cwebp.compressions).containsExactly(MICROFILM_PNG to RESOURCES_WEBP)
    assertThat(fileSystem.exists(path = RESOURCES_PNG)).isFalse()
    assertThat(fileSystem.exists(path = RESOURCES_WEBP)).isTrue()
    assertThat(fileSystem.exists(path = MICROFILM_PNG)).isTrue()
    assertThat(result).isEqualTo(Success(microfilmManifestEntry = LOSSY_MANIFEST_ENTRY))
  }

  @Test
  fun `compress succeeds with microfilm manifest entry`() {
    val result =
      compressor.compress(
        imageGroup = EMPTY_IMAGE_GROUP.copy(microfilmManifestEntry = LOSSY_MANIFEST_ENTRY),
        imageSettings = LOSSY_COMPRESS,
      )

    assertThat(cwebp.compressions).isEmpty()
    assertThat(fileSystem.exists(path = RESOURCES_PNG)).isFalse()
    assertThat(fileSystem.exists(path = RESOURCES_WEBP)).isFalse()
    assertThat(fileSystem.exists(path = MICROFILM_PNG)).isFalse()
    assertThat(result).isEqualTo(Success(microfilmManifestEntry = null))
  }

  @Test
  fun `compress succeeds when the png hash is incorrect`() {
    fileSystem.write(file = RESOURCES_WEBP) { write(WEBP_CONTENT.encodeUtf8()) }
    fileSystem.write(file = MICROFILM_PNG) { write(PNG_CONTENT.encodeUtf8()) }

    val result =
      compressor.compress(
        imageGroup =
          LOSSY_IMAGE_GROUP.copy(
            microfilmManifestEntry = LOSSY_MANIFEST_ENTRY.copy(sourceSha256 = "incorrect")
          ),
        imageSettings = LOSSY_COMPRESS,
      )

    assertThat(cwebp.compressions).containsExactly(MICROFILM_PNG to RESOURCES_WEBP)
    assertThat(result).isEqualTo(Success(microfilmManifestEntry = LOSSY_MANIFEST_ENTRY))
  }

  @Test
  fun `compress succeeds when the webp hash is incorrect`() {
    fileSystem.write(file = RESOURCES_WEBP) { write(WEBP_CONTENT.encodeUtf8()) }
    fileSystem.write(file = MICROFILM_PNG) { write(PNG_CONTENT.encodeUtf8()) }

    val result =
      compressor.compress(
        imageGroup =
          LOSSY_IMAGE_GROUP.copy(
            microfilmManifestEntry = LOSSY_MANIFEST_ENTRY.copy(compressedSha256 = "incorrect")
          ),
        imageSettings = LOSSY_COMPRESS,
      )

    assertThat(cwebp.compressions).containsExactly(MICROFILM_PNG to RESOURCES_WEBP)
    assertThat(result).isEqualTo(Success(microfilmManifestEntry = LOSSY_MANIFEST_ENTRY))
  }

  @Test
  fun `compress succeeds when the compression settings are incorrect`() {
    fileSystem.write(file = RESOURCES_WEBP) { write(WEBP_CONTENT.encodeUtf8()) }
    fileSystem.write(file = MICROFILM_PNG) { write(PNG_CONTENT.encodeUtf8()) }

    val result =
      compressor.compress(imageGroup = LOSSY_IMAGE_GROUP, imageSettings = LOSSLESS_COMPRESS)

    assertThat(cwebp.compressions).containsExactly(MICROFILM_PNG to RESOURCES_WEBP)
    assertThat(result).isEqualTo(Success(microfilmManifestEntry = LOSSLESS_MANIFEST_ENTRY))
  }

  @Test
  fun `compress is a no-op when everything is up to date`() {
    fileSystem.write(file = RESOURCES_WEBP) { write(WEBP_CONTENT.encodeUtf8()) }
    fileSystem.write(file = MICROFILM_PNG) { write(PNG_CONTENT.encodeUtf8()) }

    val result = compressor.compress(imageGroup = LOSSY_IMAGE_GROUP, imageSettings = LOSSY_COMPRESS)

    assertThat(cwebp.compressions).isEmpty()
    assertThat(result).isEqualTo(Success(microfilmManifestEntry = LOSSY_MANIFEST_ENTRY))
  }
}
