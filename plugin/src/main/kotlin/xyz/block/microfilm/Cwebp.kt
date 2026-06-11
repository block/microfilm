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

import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.io.resolve
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.process.ExecOperations

/** A wrapper around the cwebp executable. */
internal class Cwebp(
  private val execOperations: ExecOperations,
  private val directory: ConfigurableFileCollection,
) {
  private val executable by lazy { directory.singleFile.resolve("cwebp") }

  /** Returns the current cweb version. */
  fun getVersion(): String {
    val output = ByteArrayOutputStream()
    execOperations.exec { action ->
      action.commandLine(executable.absolutePath, "-version")
      action.standardOutput = output
    }
    return output.toString().lines().first().trim()
  }

  /** Compresses the source PNG image to the destination WebP using the given settings. */
  fun compress(imageSettings: ImageSettings.Compress, sourcePng: File, destinationWebp: File) {
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

          add("-o")
          add(destinationWebp.absolutePath)
          add(sourcePng.absolutePath)
        }
      )
    }
  }
}
