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
package xyz.block.microfilm.verification

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import okio.ByteString.Companion.encodeUtf8
import okio.fakefilesystem.FakeFileSystem
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import xyz.block.microfilm.ImageGroupFixtures.EMPTY_IMAGE_GROUP
import xyz.block.microfilm.ImageGroupFixtures.KEY
import xyz.block.microfilm.ImageGroupFixtures.LOSSLESS_COMPRESS
import xyz.block.microfilm.ImageGroupFixtures.LOSSY_COMPRESS
import xyz.block.microfilm.ImageGroupFixtures.LOSSY_IMAGE_GROUP
import xyz.block.microfilm.ImageGroupFixtures.LOSSY_MANIFEST_ENTRY
import xyz.block.microfilm.ImageGroupFixtures.MICROFILM_PNG
import xyz.block.microfilm.ImageGroupFixtures.PNG_CONTENT
import xyz.block.microfilm.ImageGroupFixtures.RESOURCES_PNG
import xyz.block.microfilm.ImageGroupFixtures.RESOURCES_WEBP
import xyz.block.microfilm.ImageGroupFixtures.WEBP_CONTENT
import xyz.block.microfilm.cwebp.FakeCwebp
import xyz.block.microfilm.verification.CompressVerifier.Failure
import xyz.block.microfilm.verification.Verifier.Result.Success

class CompressVerifierTest {
  private val fileSystem =
    FakeFileSystem().apply {
      createDirectories(dir = RESOURCES_PNG.parent!!)
      createDirectories(dir = MICROFILM_PNG.parent!!)
    }
  private val cwebp =
    FakeCwebp(
      fileSystem = fileSystem,
      convertToWebp = { error("Unexpected attempt to convert image to webp") },
    )

  private val verifier = CompressVerifier(cwebp = cwebp, fileSystem = fileSystem)

  @AfterEach
  fun tearDown() {
    fileSystem.checkNoOpenFiles()
  }

  @Test
  fun `verify fails with resources png`() {
    val result =
      verifier.verify(
        imageGroup = EMPTY_IMAGE_GROUP.copy(resourcesPng = RESOURCES_PNG),
        imageSettings = LOSSY_COMPRESS,
      )

    assertThat(result).isInstanceOf<Failure>().all {
      isEqualTo(
        Failure(
          key = KEY,
          hasResourcesPng = true,
          hasResourcesWebp = false,
          hasMicrofilmPng = false,
          hasMicrofilmManifestEntry = false,
          hasCorrectResourcesWebpHash = false,
          hasCorrectMicrofilmPngHash = false,
          hasCorrectCompressionSettings = false,
        )
      )

      prop(Failure::description)
        .isEqualTo(
          """
          Found an image that is covered by a `compress` rule in the Gradle configuration but is not fully compressed: drawable/photo
          [✗] There is a PNG in the resources directory when none is expected
          [✗] There is no WebP in the resources directory when one is expected
          [✗] There is no PNG in the microfilm directory when one is expected
          [✗] There is no entry in the microfilm manifest when one is expected
          To fix this failure, run the `compressMicrofilm` task.
          """
            .trimIndent()
        )
    }
  }

  @Test
  fun `verify fails with resources webp`() {
    val result =
      verifier.verify(
        imageGroup = EMPTY_IMAGE_GROUP.copy(resourcesWebp = RESOURCES_WEBP),
        imageSettings = LOSSY_COMPRESS,
      )

    assertThat(result).isInstanceOf<Failure>().all {
      isEqualTo(
        Failure(
          key = KEY,
          hasResourcesPng = false,
          hasResourcesWebp = true,
          hasMicrofilmPng = false,
          hasMicrofilmManifestEntry = false,
          hasCorrectResourcesWebpHash = false,
          hasCorrectMicrofilmPngHash = false,
          hasCorrectCompressionSettings = false,
        )
      )

      prop(Failure::description)
        .isEqualTo(
          """
          Found an image that is covered by a `compress` rule in the Gradle configuration but is not fully compressed: drawable/photo
          [✓] There is no PNG in the resources directory
          [✗] There is a WebP in the resources directory, but there is no manifest entry to compare its hash against
          [✗] There is no PNG in the microfilm directory when one is expected
          [✗] There is no entry in the microfilm manifest when one is expected
          To fix this failure, run the `compressMicrofilm` task.
          """
            .trimIndent()
        )
    }
  }

  @Test
  fun `verify fails with microfilm png`() {
    val result =
      verifier.verify(
        imageGroup = EMPTY_IMAGE_GROUP.copy(microfilmPng = MICROFILM_PNG),
        imageSettings = LOSSY_COMPRESS,
      )

    assertThat(result).isInstanceOf<Failure>().all {
      isEqualTo(
        Failure(
          key = KEY,
          hasResourcesPng = false,
          hasResourcesWebp = false,
          hasMicrofilmPng = true,
          hasMicrofilmManifestEntry = false,
          hasCorrectResourcesWebpHash = false,
          hasCorrectMicrofilmPngHash = false,
          hasCorrectCompressionSettings = false,
        )
      )

      prop(Failure::description)
        .isEqualTo(
          """
          Found an image that is covered by a `compress` rule in the Gradle configuration but is not fully compressed: drawable/photo
          [✓] There is no PNG in the resources directory
          [✗] There is no WebP in the resources directory when one is expected
          [✗] There is a PNG in the microfilm directory, but there is no manifest entry to compare its hash against
          [✗] There is no entry in the microfilm manifest when one is expected
          To fix this failure, run the `compressMicrofilm` task.
          """
            .trimIndent()
        )
    }
  }

  @Test
  fun `verify fails with microfilm manifest entry`() {
    val result =
      verifier.verify(
        imageGroup = EMPTY_IMAGE_GROUP.copy(microfilmManifestEntry = LOSSY_MANIFEST_ENTRY),
        imageSettings = LOSSY_COMPRESS,
      )

    assertThat(result).isInstanceOf<Failure>().all {
      isEqualTo(
        Failure(
          key = KEY,
          hasResourcesPng = false,
          hasResourcesWebp = false,
          hasMicrofilmPng = false,
          hasMicrofilmManifestEntry = true,
          hasCorrectResourcesWebpHash = false,
          hasCorrectMicrofilmPngHash = false,
          hasCorrectCompressionSettings = true,
        )
      )

      prop(Failure::description)
        .isEqualTo(
          """
          Found an image that is covered by a `compress` rule in the Gradle configuration but is not fully compressed: drawable/photo
          [✓] There is no PNG in the resources directory
          [✗] There is no WebP in the resources directory when one is expected
          [✗] There is no PNG in the microfilm directory when one is expected
          [✓] There is an entry in the microfilm manifest, with the correct compression settings
          To fix this failure, run the `compressMicrofilm` task.
          """
            .trimIndent()
        )
    }
  }

  @Test
  fun `verify fails when the png hash is incorrect`() {
    fileSystem.write(file = RESOURCES_WEBP) { write(byteString = WEBP_CONTENT.encodeUtf8()) }
    fileSystem.write(file = MICROFILM_PNG) { write(byteString = PNG_CONTENT.encodeUtf8()) }

    val result =
      verifier.verify(
        imageGroup =
          LOSSY_IMAGE_GROUP.copy(
            microfilmManifestEntry = LOSSY_MANIFEST_ENTRY.copy(sourceSha256 = "incorrect")
          ),
        imageSettings = LOSSY_COMPRESS,
      )

    assertThat(result).isInstanceOf<Failure>().all {
      isEqualTo(
        Failure(
          key = KEY,
          hasResourcesPng = false,
          hasResourcesWebp = true,
          hasMicrofilmPng = true,
          hasMicrofilmManifestEntry = true,
          hasCorrectResourcesWebpHash = true,
          hasCorrectMicrofilmPngHash = false,
          hasCorrectCompressionSettings = true,
        )
      )

      prop(Failure::description)
        .isEqualTo(
          """
          Found an image that is covered by a `compress` rule in the Gradle configuration but is not fully compressed: drawable/photo
          [✓] There is no PNG in the resources directory
          [✓] There is a WebP in the resources directory with the correct hash
          [✗] There is a PNG in the microfilm directory, but it has an incorrect hash
          [✓] There is an entry in the microfilm manifest, with the correct compression settings
          To fix this failure, run the `compressMicrofilm` task.
          """
            .trimIndent()
        )
    }
  }

  @Test
  fun `verify fails when the webp hash is incorrect`() {
    fileSystem.write(file = RESOURCES_WEBP) { write(byteString = WEBP_CONTENT.encodeUtf8()) }
    fileSystem.write(file = MICROFILM_PNG) { write(byteString = PNG_CONTENT.encodeUtf8()) }

    val result =
      verifier.verify(
        imageGroup =
          LOSSY_IMAGE_GROUP.copy(
            microfilmManifestEntry = LOSSY_MANIFEST_ENTRY.copy(compressedSha256 = "incorrect")
          ),
        imageSettings = LOSSY_COMPRESS,
      )

    assertThat(result).isInstanceOf<Failure>().all {
      isEqualTo(
        Failure(
          key = KEY,
          hasResourcesPng = false,
          hasResourcesWebp = true,
          hasMicrofilmPng = true,
          hasMicrofilmManifestEntry = true,
          hasCorrectResourcesWebpHash = false,
          hasCorrectMicrofilmPngHash = true,
          hasCorrectCompressionSettings = true,
        )
      )

      prop(Failure::description)
        .isEqualTo(
          """
          Found an image that is covered by a `compress` rule in the Gradle configuration but is not fully compressed: drawable/photo
          [✓] There is no PNG in the resources directory
          [✗] There is a WebP in the resources directory, but it has an incorrect hash
          [✓] There is a PNG in the microfilm directory with the correct hash
          [✓] There is an entry in the microfilm manifest, with the correct compression settings
          To fix this failure, run the `compressMicrofilm` task.
          """
            .trimIndent()
        )
    }
  }

  @Test
  fun `verify fails when the compression settings are incorrect`() {
    fileSystem.write(file = RESOURCES_WEBP) { write(byteString = WEBP_CONTENT.encodeUtf8()) }
    fileSystem.write(file = MICROFILM_PNG) { write(byteString = PNG_CONTENT.encodeUtf8()) }

    val result = verifier.verify(imageGroup = LOSSY_IMAGE_GROUP, imageSettings = LOSSLESS_COMPRESS)

    assertThat(result).isInstanceOf<Failure>().all {
      isEqualTo(
        Failure(
          key = KEY,
          hasResourcesPng = false,
          hasResourcesWebp = true,
          hasMicrofilmPng = true,
          hasMicrofilmManifestEntry = true,
          hasCorrectResourcesWebpHash = true,
          hasCorrectMicrofilmPngHash = true,
          hasCorrectCompressionSettings = false,
        )
      )

      prop(Failure::description)
        .isEqualTo(
          """
          Found an image that is covered by a `compress` rule in the Gradle configuration but is not fully compressed: drawable/photo
          [✓] There is no PNG in the resources directory
          [✓] There is a WebP in the resources directory with the correct hash
          [✓] There is a PNG in the microfilm directory with the correct hash
          [✗] There is an entry in the microfilm manifest, but it has incorrect compression settings
          To fix this failure, run the `compressMicrofilm` task.
          """
            .trimIndent()
        )
    }
  }

  @Test
  fun `verify succeeds when everything is up to date`() {
    fileSystem.write(file = RESOURCES_WEBP) { write(byteString = WEBP_CONTENT.encodeUtf8()) }
    fileSystem.write(file = MICROFILM_PNG) { write(byteString = PNG_CONTENT.encodeUtf8()) }

    val result = verifier.verify(imageGroup = LOSSY_IMAGE_GROUP, imageSettings = LOSSY_COMPRESS)

    assertThat(result).isEqualTo(Success(key = KEY))
  }
}
