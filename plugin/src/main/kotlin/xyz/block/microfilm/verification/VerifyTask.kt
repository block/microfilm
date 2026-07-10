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

import javax.inject.Inject
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault
import xyz.block.microfilm.ImageRule
import xyz.block.microfilm.cwebp.RealCwebp
import xyz.block.microfilm.scanning.RealScanner
import xyz.block.microfilm.verification.Verifier.Result.Failure

@DisableCachingByDefault(because = "This task produces no outputs")
internal abstract class VerifyTask @Inject constructor(private val execOperations: ExecOperations) :
  DefaultTask() {
  @get:InputFiles
  @get:PathSensitive(RELATIVE)
  abstract val cwebpDirectory: ConfigurableFileCollection

  @get:Internal abstract val imageRules: ListProperty<ImageRule>

  @get:Internal abstract val microfilmDirectory: DirectoryProperty

  @get:Internal abstract val resourcesDirectory: DirectoryProperty

  private val resourcesDirectoryPath by lazy { resourcesDirectory.get().asFile.toOkioPath() }
  private val microfilmDirectoryPath by lazy { microfilmDirectory.get().asFile.toOkioPath() }

  @TaskAction
  fun verify() {
    val cwebp = RealCwebp(execOperations = execOperations, directory = cwebpDirectory)
    val fileSystem = FileSystem.SYSTEM
    val verifier =
      TypedVerifier(
        compressVerifier = CompressVerifier(cwebp = cwebp, fileSystem = fileSystem),
        excludeVerifier = ExcludeVerifier(),
        unspecifiedVerifier = UnspecifiedVerifier(),
      )

    val failures =
      verifier
        .verify(
          scanner =
            RealScanner(
              fileSystem = fileSystem,
              resourcesDirectory = resourcesDirectoryPath,
              microfilmDirectory = microfilmDirectoryPath,
            ),
          imageRules = imageRules.get(),
          resourcesDirectory = resourcesDirectoryPath,
          microfilmDirectory = microfilmDirectoryPath,
        )
        .filterIsInstance<Failure>()

    if (failures.isNotEmpty()) {
      throw GradleException(
        buildString {
          appendLine("Microfilm verification failed for ${failures.size} image(s):")
          appendLine(failures.joinToString(separator = "\n\n") { failures -> failures.description })
        }
      )
    }
  }
}
