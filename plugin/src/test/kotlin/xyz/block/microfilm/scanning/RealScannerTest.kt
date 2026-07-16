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
package xyz.block.microfilm.scanning

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import kotlinx.serialization.json.Json
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
import xyz.block.microfilm.ImageGroupFixtures.WEBP_CONTENT
import xyz.block.microfilm.Manifest

class RealScannerTest {
  private val fileSystem =
    FakeFileSystem().apply {
      createDirectories(dir = RESOURCES_PNG.parent!!)
      createDirectories(dir = MICROFILM_PNG.parent!!)
    }

  private val scanner =
    RealScanner(
      fileSystem = fileSystem,
      resourcesDirectory = RESOURCES_DIRECTORY,
      microfilmDirectory = MICROFILM_DIRECTORY,
    )

  @AfterEach
  fun tearDown() {
    fileSystem.checkNoOpenFiles()
  }

  @Test
  fun `scan finds nothing when empty`() {
    fileSystem.deleteRecursively(fileOrDirectory = RESOURCES_PNG.parent!!)
    fileSystem.deleteRecursively(fileOrDirectory = MICROFILM_PNG.parent!!)

    assertThat(scanner.scan()).isEmpty()
  }

  @Test
  fun `scan finds a resources png`() {
    fileSystem.write(file = RESOURCES_PNG) { write(PNG_CONTENT.encodeUtf8()) }

    assertThat(scanner.scan()).containsExactly(EMPTY_IMAGE_GROUP.copy(resourcesPng = RESOURCES_PNG))
  }

  @Test
  fun `scan finds a resources webp`() {
    fileSystem.write(file = RESOURCES_WEBP) { write(WEBP_CONTENT.encodeUtf8()) }

    assertThat(scanner.scan())
      .containsExactly(EMPTY_IMAGE_GROUP.copy(resourcesWebp = RESOURCES_WEBP))
  }

  @Test
  fun `scan finds a microfilm png`() {
    fileSystem.write(file = MICROFILM_PNG) { write(PNG_CONTENT.encodeUtf8()) }

    assertThat(scanner.scan()).containsExactly(EMPTY_IMAGE_GROUP.copy(microfilmPng = MICROFILM_PNG))
  }

  @Test
  fun `scan finds a manifest entry`() {
    writeManifest(LOSSY_MANIFEST_ENTRY)

    assertThat(scanner.scan())
      .containsExactly(EMPTY_IMAGE_GROUP.copy(microfilmManifestEntry = LOSSY_MANIFEST_ENTRY))
  }

  @Test
  fun `scan groups items with the same key`() {
    fileSystem.write(file = RESOURCES_PNG) { write(PNG_CONTENT.encodeUtf8()) }
    fileSystem.write(file = RESOURCES_WEBP) { write(WEBP_CONTENT.encodeUtf8()) }
    fileSystem.write(file = MICROFILM_PNG) { write(PNG_CONTENT.encodeUtf8()) }
    writeManifest(LOSSY_MANIFEST_ENTRY)

    assertThat(scanner.scan())
      .containsExactly(
        ImageGroup(
          key = KEY,
          resourcesPng = RESOURCES_PNG,
          resourcesWebp = RESOURCES_WEBP,
          microfilmPng = MICROFILM_PNG,
          microfilmManifestEntry = LOSSY_MANIFEST_ENTRY,
        )
      )
  }

  private fun writeManifest(vararg entries: Manifest.Entry) {
    fileSystem.write(file = MICROFILM_DIRECTORY.resolve(child = "manifest.json")) {
      writeUtf8(
        Json.encodeToString(
          serializer = Manifest.serializer(),
          value = Manifest(entries = entries.toList()),
        )
      )
    }
  }
}
