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

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import okio.ByteString.Companion.encodeUtf8
import okio.fakefilesystem.FakeFileSystem
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import xyz.block.microfilm.ImageGroupFixtures.EMPTY_IMAGE_GROUP
import xyz.block.microfilm.ImageGroupFixtures.KEY
import xyz.block.microfilm.ImageGroupFixtures.LOSSY_MANIFEST_ENTRY
import xyz.block.microfilm.ImageGroupFixtures.MICROFILM_DIRECTORY
import xyz.block.microfilm.ImageGroupFixtures.MICROFILM_PNG
import xyz.block.microfilm.ImageGroupFixtures.PNG_CONTENT
import xyz.block.microfilm.ImageGroupFixtures.RESOURCES_DIRECTORY
import xyz.block.microfilm.ImageGroupFixtures.RESOURCES_PNG
import xyz.block.microfilm.ImageGroupFixtures.RESOURCES_WEBP
import xyz.block.microfilm.ImageSettings.Unspecified
import xyz.block.microfilm.compression.Compressor.Result.Success
import xyz.block.microfilm.compression.UnspecifiedCompressor.Failure

class UnspecifiedCompressorTest {
  private val fileSystem = FakeFileSystem()

  private val compressor =
    UnspecifiedCompressor(
      fileSystem = fileSystem,
      resourcesDirectory = RESOURCES_DIRECTORY,
      microfilmDirectory = MICROFILM_DIRECTORY,
    )

  @AfterEach
  fun tearDown() {
    fileSystem.checkNoOpenFiles()
  }

  @Test
  fun `compress fails with resources png`() {
    val result =
      compressor.compress(
        imageGroup = EMPTY_IMAGE_GROUP.copy(resourcesPng = RESOURCES_PNG),
        imageSettings = Unspecified,
      )

    assertThat(result).isInstanceOf<Failure>().all {
      isEqualTo(Failure(key = KEY, hasResourcesPng = true, hasResourcesWebp = false))

      prop(Failure::description)
        .isEqualTo(
          """
          Found an image that is covered by neither a `compress` nor an `exclude` rule in the Gradle configuration: drawable/photo
          [✗] There is a PNG in the resources directory when none is expected
          [✓] There is no WebP in the resources directory
          To fix this failure, add a `compress` or `exclude` rule that covers this image, or delete the image, then rerun the `compressMicrofilm` task.
          """
            .trimIndent()
        )
    }
  }

  @Test
  fun `compress fails with resources webp`() {
    val result =
      compressor.compress(
        imageGroup = EMPTY_IMAGE_GROUP.copy(resourcesWebp = RESOURCES_WEBP),
        imageSettings = Unspecified,
      )

    assertThat(result).isInstanceOf<Failure>().all {
      isEqualTo(Failure(key = KEY, hasResourcesPng = false, hasResourcesWebp = true))

      prop(Failure::description)
        .isEqualTo(
          """
          Found an image that is covered by neither a `compress` nor an `exclude` rule in the Gradle configuration: drawable/photo
          [✓] There is no PNG in the resources directory
          [✗] There is a WebP in the resources directory when none is expected
          To fix this failure, add a `compress` or `exclude` rule that covers this image, or delete the image, then rerun the `compressMicrofilm` task.
          """
            .trimIndent()
        )
    }
  }

  @Test
  fun `compress fails with microfilm png`() {
    fileSystem.createDirectories(dir = MICROFILM_PNG.parent!!)
    fileSystem.write(file = MICROFILM_PNG) { write(byteString = PNG_CONTENT.encodeUtf8()) }

    val result =
      compressor.compress(
        imageGroup = EMPTY_IMAGE_GROUP.copy(microfilmPng = MICROFILM_PNG),
        imageSettings = Unspecified,
      )

    assertThat(fileSystem.exists(MICROFILM_PNG)).isFalse()
    assertThat(fileSystem.read(file = RESOURCES_PNG) { readUtf8() }).isEqualTo(PNG_CONTENT)
    assertThat(result).isInstanceOf<Failure>().all {
      isEqualTo(Failure(key = KEY, hasResourcesPng = true, hasResourcesWebp = false))

      prop(Failure::description)
        .isEqualTo(
          """
          Found an image that is covered by neither a `compress` nor an `exclude` rule in the Gradle configuration: drawable/photo
          [✗] There is a PNG in the resources directory when none is expected
          [✓] There is no WebP in the resources directory
          To fix this failure, add a `compress` or `exclude` rule that covers this image, or delete the image, then rerun the `compressMicrofilm` task.
          """
            .trimIndent()
        )
    }
  }

  @Test
  fun `compress succeeds with microfilm manifest entry`() {
    val result =
      compressor.compress(
        imageGroup = EMPTY_IMAGE_GROUP.copy(microfilmManifestEntry = LOSSY_MANIFEST_ENTRY),
        imageSettings = Unspecified,
      )

    assertThat(result).isEqualTo(Success(microfilmManifestEntry = null))
  }
}
