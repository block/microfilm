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

import java.io.ByteArrayOutputStream
import kotlin.io.resolve
import okio.Path
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.process.ExecOperations
import xyz.block.microfilm.ImageSettings.Compress

/** A [Cwebp] backed by [ExecOperations] and a cwebp binary executable. */
internal class RealCwebp(
  private val execOperations: ExecOperations,
  private val directory: ConfigurableFileCollection,
) : Cwebp {
  private val executable by lazy { directory.singleFile.resolve("cwebp") }
  private val _version by lazy {
    val output = ByteArrayOutputStream()
    execOperations.exec { action ->
      action.commandLine(executable.absolutePath, "-version")
      action.standardOutput = output
    }
    output.toString().lines().first().trim()
  }

  override fun getVersion(): String = _version

  override fun compress(imageSettings: Compress, sourcePng: Path, destinationWebp: Path) {
    execOperations.exec { action ->
      action.commandLine(
        buildList {
          add(executable.absolutePath)
          if (imageSettings.lossless) {
            add("-lossless")
          }

          imageSettings.compressionFactor?.let { compressionFactor ->
            add("-q")
            add(compressionFactor.toString())
          }

          imageSettings.compressionMethod?.let { compressionMethod ->
            add("-m")
            add(compressionMethod.toString())
          }

          imageSettings.metadata?.let { metadata ->
            add("-metadata")
            add(metadata.toString())
          }

          add("-o")
          add(destinationWebp.toString())
          add(sourcePng.toString())
        }
      )
    }
  }
}
