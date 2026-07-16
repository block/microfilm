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

import java.util.zip.ZipInputStream
import org.gradle.api.artifacts.transform.CacheableTransform
import org.gradle.api.artifacts.transform.InputArtifact
import org.gradle.api.artifacts.transform.TransformAction
import org.gradle.api.artifacts.transform.TransformOutputs
import org.gradle.api.artifacts.transform.TransformParameters.None
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.RELATIVE

/** Extracts the cwebp binary from a platform-specific JAR. */
@CacheableTransform
internal abstract class ExtractCwebpBinary : TransformAction<None> {
  @get:InputArtifact
  @get:PathSensitive(RELATIVE)
  abstract val inputJar: Provider<FileSystemLocation>

  override fun transform(outputs: TransformOutputs) {
    val inputJarFile = inputJar.get().asFile
    val outputExecutableFile = outputs.dir(inputJarFile.nameWithoutExtension).resolve("cwebp")
    ZipInputStream(inputJarFile.inputStream().buffered()).use { zip ->
      while (true) {
        val entry = zip.nextEntry ?: break
        if (!entry.isDirectory && entry.name.substringAfterLast('/') == "cwebp") {
          outputExecutableFile.outputStream().buffered().use { zip.copyTo(it) }
          outputExecutableFile.setExecutable(true)
          break
        }
      }
    }
  }
}
