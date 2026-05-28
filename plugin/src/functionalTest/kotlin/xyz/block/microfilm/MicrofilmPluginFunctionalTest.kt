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

import com.autonomousapps.kit.AbstractGradleProject.Companion.PLUGIN_UNDER_TEST_VERSION
import com.autonomousapps.kit.GradleBuilder
import com.autonomousapps.kit.GradleProject
import com.autonomousapps.kit.gradle.Plugin
import com.autonomousapps.kit.truth.TestKitTruth.Companion.assertThat
import com.google.common.truth.Truth.assertThat
import java.io.File
import org.gradle.testkit.runner.BuildResult
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class MicrofilmPluginFunctionalTest {
  private val agpVersion = System.getProperty("agpVersion")!!

  private val androidAppPlugin = Plugin("com.android.application", agpVersion)
  private val androidLibPlugin = Plugin("com.android.library", agpVersion)
  private val microfilmPlugin = Plugin("xyz.block.microfilm", PLUGIN_UNDER_TEST_VERSION)

  private fun androidAppProject(additions: String = MICROFILM_CONFIGURATION) =
    MicrofilmProject()
      .androidApp(
        androidAppPlugin = androidAppPlugin,
        microfilmPlugin = microfilmPlugin,
        additions = additions,
      )

  private fun androidLibProject(additions: String = MICROFILM_CONFIGURATION) =
    MicrofilmProject()
      .androidLib(
        androidLibPlugin = androidLibPlugin,
        microfilmPlugin = microfilmPlugin,
        additions = additions,
      )

  private fun vanillaProject() = MicrofilmProject().vanilla(microfilmPlugin = microfilmPlugin)

  @Test
  fun `plugin applies to android application project`() {
    val project = androidAppProject()
    val result = project.build(":app:tasks", "--group=microfilm")

    assertThat(result.output).contains("compressMicrofilmDebug")
    assertThat(result.output).contains("compressMicrofilmMain")
    assertThat(result.output).contains("verifyMicrofilmDebug")
    assertThat(result.output).contains("verifyMicrofilmMain")
  }

  @Test
  fun `plugin applies to android library project`() {
    val project = androidLibProject()
    val result = project.build(":lib:tasks", "--group=microfilm")

    assertThat(result.output).contains("compressMicrofilmDebug")
    assertThat(result.output).contains("compressMicrofilmMain")
    assertThat(result.output).contains("verifyMicrofilmDebug")
    assertThat(result.output).contains("verifyMicrofilmMain")
  }

  @Test
  fun `plugin does not apply to non-android project`() {
    val project = vanillaProject()
    val result = GradleBuilder.buildAndFail(project.rootDir, "tasks", "--group=microfilm")

    assertThat(result.output)
      .contains(
        "Microfilm requires either the 'com.android.application' or 'com.android.library' plugin."
      )
  }

  @Test
  fun `compress task is skipped when source set is empty`() {
    val project = androidLibProject()
    val result = project.build(":lib:compressMicrofilmMain")

    assertThat(result).task(":lib:compressMicrofilmMain").skipped()
  }

  @Test
  fun `compress task succeeds when source set has orphaned manifest in microfilm directory`() {
    val project = androidLibProject()
    MANIFEST_FIXTURE.copyToDirectory(directory = project.libMicrofilmDirectory)

    val result = project.build(":lib:compressMicrofilmMain")

    assertThat(result).task(":lib:compressMicrofilmMain").succeeded()
    assertThat(project.libResourcesDrawableDirectory.containsPng()).isFalse()
    assertThat(project.libResourcesDrawableDirectory.containsWebp()).isFalse()
    assertThat(project.libMicrofilmDirectory.containsManifest()).isFalse()
    assertThat(project.libMicrofilmDrawableDirectory.containsPng()).isFalse()
  }

  @Test
  fun `compress task succeeds when source set has uncompressed png image in resources directory`() {
    val project = androidLibProject()
    PNG_FIXTURE.copyToDirectory(directory = project.libResourcesDrawableDirectory)

    val result = project.build(":lib:compressMicrofilmMain")

    assertThat(result).task(":lib:compressMicrofilmMain").succeeded()
    assertThat(project.libResourcesDrawableDirectory.containsPng()).isFalse()
    assertThat(project.libResourcesDrawableDirectory.containsWebp()).isTrue()
    assertThat(project.libMicrofilmDirectory.containsManifest()).isTrue()
    assertThat(project.libMicrofilmDrawableDirectory.containsPng()).isTrue()
  }

  @Test
  fun `compress task succeeds when source set has uncompressed png image in microfilm directory`() {
    val project = androidLibProject()
    PNG_FIXTURE.copyToDirectory(directory = project.libMicrofilmDrawableDirectory)

    val result = project.build(":lib:compressMicrofilmMain")

    assertThat(result).task(":lib:compressMicrofilmMain").succeeded()
    assertThat(project.libResourcesDrawableDirectory.containsPng()).isFalse()
    assertThat(project.libResourcesDrawableDirectory.containsWebp()).isTrue()
    assertThat(project.libMicrofilmDirectory.containsManifest()).isTrue()
    assertThat(project.libMicrofilmDrawableDirectory.containsPng()).isTrue()
  }

  @Test
  fun `compress task succeeds when source set has compressed webp image in resources directory`() {
    val project = androidLibProject()
    MANIFEST_FIXTURE.copyToDirectory(directory = project.libMicrofilmDirectory)
    PNG_FIXTURE.copyToDirectory(directory = project.libMicrofilmDrawableDirectory)
    WEBP_FIXTURE.copyToDirectory(directory = project.libResourcesDrawableDirectory)

    val result = project.build(":lib:compressMicrofilmMain")

    assertThat(result).task(":lib:compressMicrofilmMain").succeeded()
    assertThat(project.libResourcesDrawableDirectory.containsPng()).isFalse()
    assertThat(project.libResourcesDrawableDirectory.containsWebp()).isTrue()
    assertThat(project.libMicrofilmDirectory.containsManifest()).isTrue()
    assertThat(project.libMicrofilmDrawableDirectory.containsPng()).isTrue()
  }

  @Test
  fun `compress task skipped when source set has independent webp image in resources directory`() {
    val project = androidLibProject()
    WEBP_FIXTURE.copyToDirectory(directory = project.libResourcesDrawableDirectory)

    val result = project.build(":lib:compressMicrofilmMain")

    assertThat(result).task(":lib:compressMicrofilmMain").skipped()
  }

  @Test
  fun `compress task succeeds when images match different rules`() {
    val project = androidLibProject()
    PNG_FIXTURE.copyToDirectory(
      directory = project.libResourcesDrawableDirectory,
      name = PNG_LOSSLESS_NAME,
    )
    PNG_FIXTURE.copyToDirectory(
      directory = project.libResourcesDrawableDirectory,
      name = PNG_LOSSY_NAME,
    )
    PNG_FIXTURE.copyToDirectory(
      directory = project.libResourcesDrawableDirectory,
      name = PNG_EXCLUDED_NAME,
    )

    val result = project.build(":lib:compressMicrofilmMain")

    assertThat(result).task(":lib:compressMicrofilmMain").succeeded()
    assertThat(
        project.libMicrofilmDirectory.containsFileContent(
          name = MANIFEST_NAME,
          content = MANIFEST_FIXTURE_FULL,
        )
      )
      .isTrue()

    // Verify the lossless image
    assertThat(project.libResourcesDrawableDirectory.containsFile(name = PNG_LOSSLESS_NAME))
      .isFalse()
    assertThat(
        project.libResourcesDrawableDirectory.containsFileContent(
          name = WEBP_LOSSLESS_NAME,
          content = WEBP_FIXTURE,
        )
      )
      .isTrue()
    assertThat(
        project.libMicrofilmDrawableDirectory.containsFileContent(
          name = PNG_LOSSLESS_NAME,
          content = PNG_FIXTURE,
        )
      )
      .isTrue()

    // Verify the lossy image
    assertThat(project.libResourcesDrawableDirectory.containsFile(name = PNG_LOSSY_NAME)).isFalse()
    assertThat(
        project.libResourcesDrawableDirectory.containsFileContent(
          name = WEBP_LOSSY_NAME,
          content = WEBP_FIXTURE_LOSSY,
        )
      )
      .isTrue()
    assertThat(
        project.libMicrofilmDrawableDirectory.containsFileContent(
          name = PNG_LOSSY_NAME,
          content = PNG_FIXTURE,
        )
      )
      .isTrue()

    // Verify the excluded image
    assertThat(
        project.libResourcesDrawableDirectory.containsFileContent(
          name = PNG_EXCLUDED_NAME,
          content = PNG_FIXTURE,
        )
      )
      .isTrue()
    assertThat(project.libResourcesDrawableDirectory.containsFile(name = WEBP_EXCLUDED_NAME))
      .isFalse()
    assertThat(project.libMicrofilmDrawableDirectory.containsFile(name = PNG_EXCLUDED_NAME))
      .isFalse()
  }

  @Test
  fun `compress task is compatible with configuration cache`() {
    val project = androidLibProject()

    // First build stores the configuration cache
    val result1 = project.build(":lib:compressMicrofilm", "--configuration-cache")
    assertThat(result1.output).contains("Configuration cache entry stored")

    // Second build reuses the configuration cache
    val result2 = project.build(":lib:compressMicrofilm", "--configuration-cache")
    assertThat(result2.output).contains("Configuration cache entry reused")
  }

  @Test
  fun `verify task is connected to check task`() {
    val project = androidLibProject()
    val result = project.build(":lib:check", "--dry-run")

    assertThat(result.output).contains(":lib:verifyMicrofilm")
  }

  @Test
  fun `verify task is skipped when source set is empty`() {
    val project = androidLibProject()
    val result = project.build(":lib:verifyMicrofilmMain")

    assertThat(result).task(":lib:verifyMicrofilmMain").skipped()
  }

  @Test
  @Disabled("See https://linear.app/squareup/issue/TCS-698")
  fun `verify task fails when source set has orphaned manifest in microfilm directory`() {
    val project = androidLibProject()
    MANIFEST_FIXTURE.copyToDirectory(directory = project.libMicrofilmDirectory)

    val result = GradleBuilder.buildAndFail(project.rootDir, ":lib:verifyMicrofilmMain")

    assertThat(result).task(":lib:verifyMicrofilmMain").failed()
  }

  @Test
  @Disabled("See https://linear.app/squareup/issue/TCS-698")
  fun `verify task fails when source set has uncompressed png image in resources directory`() {
    val project = androidLibProject()
    PNG_FIXTURE.copyToDirectory(directory = project.libResourcesDrawableDirectory)

    val result = GradleBuilder.buildAndFail(project.rootDir, ":lib:verifyMicrofilmMain")

    assertThat(result).task(":lib:verifyMicrofilmMain").failed()
  }

  @Test
  @Disabled("See https://linear.app/squareup/issue/TCS-698")
  fun `verify task fails when source set has uncompressed png image in microfilm directory`() {
    val project = androidLibProject()
    PNG_FIXTURE.copyToDirectory(directory = project.libMicrofilmDrawableDirectory)

    val result = GradleBuilder.buildAndFail(project.rootDir, ":lib:verifyMicrofilmMain")

    assertThat(result).task(":lib:verifyMicrofilmMain").failed()
  }

  @Test
  fun `verify task succeeds when source set has compressed webp image in resources directory`() {
    val project = androidLibProject()
    MANIFEST_FIXTURE.copyToDirectory(directory = project.libMicrofilmDirectory)
    PNG_FIXTURE.copyToDirectory(directory = project.libMicrofilmDrawableDirectory)
    WEBP_FIXTURE.copyToDirectory(directory = project.libResourcesDrawableDirectory)

    val result = project.build(":lib:verifyMicrofilmMain")

    assertThat(result).task(":lib:verifyMicrofilmMain").succeeded()
  }

  @Test
  fun `verify task skipped when source set has independent webp image in resources directory`() {
    val project = androidLibProject()
    WEBP_FIXTURE_LOSSY.copyToDirectory(directory = project.libResourcesDrawableDirectory)

    val result = project.build(":lib:verifyMicrofilmMain")

    assertThat(result).task(":lib:verifyMicrofilmMain").skipped()
  }

  @Test
  fun `verify task succeeds when images match different rules`() {
    val project = androidLibProject()
    MANIFEST_FIXTURE_FULL.copyToDirectory(
      directory = project.libMicrofilmDirectory,
      name = MANIFEST_NAME,
    )
    PNG_FIXTURE.copyToDirectory(
      directory = project.libMicrofilmDrawableDirectory,
      name = PNG_LOSSLESS_NAME,
    )
    PNG_FIXTURE.copyToDirectory(
      directory = project.libMicrofilmDrawableDirectory,
      name = PNG_LOSSY_NAME,
    )
    PNG_FIXTURE.copyToDirectory(
      directory = project.libResourcesDrawableDirectory,
      name = PNG_EXCLUDED_NAME,
    )
    WEBP_FIXTURE.copyToDirectory(
      directory = project.libResourcesDrawableDirectory,
      name = WEBP_LOSSLESS_NAME,
    )
    WEBP_FIXTURE_LOSSY.copyToDirectory(
      directory = project.libResourcesDrawableDirectory,
      name = WEBP_LOSSY_NAME,
    )

    val result = project.build(":lib:verifyMicrofilmMain")

    assertThat(result).task(":lib:verifyMicrofilmMain").succeeded()
  }

  @Test
  fun `verify task is compatible with configuration cache`() {
    val project = androidLibProject()

    // First build stores the configuration cache
    val result1 = project.build(":lib:verifyMicrofilm", "--configuration-cache")
    assertThat(result1.output).contains("Configuration cache entry stored")

    // Second build reuses the configuration cache
    val result2 = project.build(":lib:verifyMicrofilm", "--configuration-cache")
    assertThat(result2.output).contains("Configuration cache entry reused")
  }

  companion object {
    private const val MANIFEST_NAME = "manifest.json"
    private const val PNG_NAME = "cash_app_green.png"
    private const val PNG_EXCLUDED_NAME = "cash_app_green_excluded.png"
    private const val PNG_LOSSLESS_NAME = "cash_app_green_lossless.png"
    private const val PNG_LOSSY_NAME = "cash_app_green_lossy.png"
    private const val WEBP_NAME = "cash_app_green.webp"
    private const val WEBP_EXCLUDED_NAME = "cash_app_green_excluded.webp"
    private const val WEBP_LOSSLESS_NAME = "cash_app_green_lossless.webp"
    private const val WEBP_LOSSY_NAME = "cash_app_green_lossy.webp"

    private val FIXTURES_DIRECTORY = File("src/functionalTest/resources")
    private val MANIFEST_FIXTURE = FIXTURES_DIRECTORY.resolve(relative = MANIFEST_NAME)
    private val MANIFEST_FIXTURE_FULL = FIXTURES_DIRECTORY.resolve(relative = "manifest_full.json")
    private val PNG_FIXTURE = FIXTURES_DIRECTORY.resolve(relative = PNG_NAME)
    private val WEBP_FIXTURE = FIXTURES_DIRECTORY.resolve(relative = WEBP_NAME)
    private val WEBP_FIXTURE_LOSSY =
      FIXTURES_DIRECTORY.resolve(relative = "cash_app_green_lossy.webp")

    private val MICROFILM_CONFIGURATION =
      """
      microfilm {
        compress { lossless = true }

        compress("**/*_lossy.png") {
          lossless = false
          compressionFactor = 100
        }

        exclude("**/*_excluded.png")
      }
      """
        .trimIndent()
  }

  private fun File.containsManifest() =
    containsFileContent(name = MANIFEST_NAME, content = MANIFEST_FIXTURE)

  private fun File.containsPng() = containsFileContent(name = PNG_NAME, content = PNG_FIXTURE)

  private fun File.containsWebp() = containsFileContent(name = WEBP_NAME, content = WEBP_FIXTURE)

  private fun File.containsFile(name: String) = findFile(name = name) != null

  private fun File.containsFileContent(name: String, content: File) =
    findFile(name = name)?.readBytes()?.contentEquals(content.readBytes()) ?: false

  private fun File.findFile(name: String) =
    walk().firstOrNull { file -> file.isFile && file.name == name }

  private fun File.copyToDirectory(directory: File, name: String? = null) =
    copyTo(target = directory.resolve(relative = name ?: this.name))

  private val GradleProject.libResourcesDrawableDirectory
    get() = directory(relative = "lib/src/main/res/drawable")

  private val GradleProject.libMicrofilmDirectory
    get() = directory(relative = "lib/src/main/microfilm")

  private val GradleProject.libMicrofilmDrawableDirectory
    get() = directory(relative = "lib/src/main/microfilm/drawable")

  private fun GradleProject.build(vararg args: String): BuildResult =
    GradleBuilder.build(rootDir, *args)

  private fun GradleProject.directory(relative: String) =
    rootDir.resolve(relative = relative).apply { mkdirs() }
}
