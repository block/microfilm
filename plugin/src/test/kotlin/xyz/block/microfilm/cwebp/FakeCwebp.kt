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
package xyz.block.microfilm.cwebp

import okio.ByteString.Companion.encodeUtf8
import okio.Path
import okio.fakefilesystem.FakeFileSystem
import xyz.block.microfilm.ImageSettings.Compress

class FakeCwebp(
  private val fileSystem: FakeFileSystem,
  private val convertToWebp: (pngContent: String) -> String,
) : Cwebp {
  val compressions = ArrayDeque<Pair<Path, Path>>()

  override fun getVersion(): String = VERSION

  override fun compress(imageSettings: Compress, sourcePng: Path, destinationWebp: Path) {
    compressions.add(sourcePng to destinationWebp)
    val pngContent = fileSystem.read(file = sourcePng) { readUtf8() }
    val webpContent = convertToWebp(pngContent)
    fileSystem.write(file = destinationWebp) { write(webpContent.encodeUtf8()) }
  }

  companion object {
    const val VERSION = "1.2.3"
  }
}
