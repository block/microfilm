package xyz.block.microfilm

import com.autonomousapps.kit.AbstractGradleProject
import com.autonomousapps.kit.GradleBuilder
import com.autonomousapps.kit.GradleProject
import com.autonomousapps.kit.gradle.Plugin
import com.autonomousapps.kit.truth.TestKitTruth.Companion.assertThat
import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.BuildResult
import org.junit.jupiter.api.Test

class MicrofilmPluginFunctionalTest {
  private val agpVersion = System.getProperty("agpVersion")!!

  private val androidAppPlugin = Plugin("com.android.application", agpVersion)
  private val androidLibPlugin = Plugin("com.android.library", agpVersion)
  private val microfilmPlugin =
    Plugin("xyz.block.microfilm", AbstractGradleProject.PLUGIN_UNDER_TEST_VERSION)

  private fun androidAppProject(additions: String = "") =
    MicrofilmProject()
      .androidApp(
        androidAppPlugin = androidAppPlugin,
        microfilmPlugin = microfilmPlugin,
        additions = additions,
      )

  private fun androidLibProject(additions: String = "") =
    MicrofilmProject()
      .androidLib(
        androidLibPlugin = androidLibPlugin,
        microfilmPlugin = microfilmPlugin,
        additions = additions,
      )

  private fun vanillaProject() = MicrofilmProject().vanilla(microfilmPlugin = microfilmPlugin)

  private fun GradleProject.build(vararg args: String): BuildResult =
    GradleBuilder.build(rootDir, *args)

  @Test
  fun `plugin applies to android application project`() {
    val project = androidAppProject()
    val result = project.build(":app:tasks", "--group=microfilm")

    assertThat(result.output).contains("compressMicrofilm")
    assertThat(result.output).contains("verifyMicrofilm")
  }

  @Test
  fun `plugin applies to android library project`() {
    val project = androidLibProject()
    val result = project.build(":lib:tasks", "--group=microfilm")

    assertThat(result.output).contains("compressMicrofilm")
    assertThat(result.output).contains("verifyMicrofilm")
  }

  @Test
  fun `registers per-source-set tasks`() {
    val project = androidAppProject()
    val result = project.build(":app:tasks", "--all", "--group=microfilm")

    assertThat(result.output).contains("compressMicrofilmMain")
    assertThat(result.output).contains("verifyMicrofilmMain")
    assertThat(result.output).contains("compressMicrofilmDebug")
    assertThat(result.output).contains("verifyMicrofilmDebug")
  }

  @Test
  fun `verify task is wired to check`() {
    val project = androidAppProject()
    val result = project.build(":app:check", "--dry-run")

    assertThat(result.output).contains(":app:verifyMicrofilm")
  }

  @Test
  fun `compress task runs successfully`() {
    val project = androidAppProject()
    val result = project.build(":app:compressMicrofilm")

    assertThat(result).task(":app:compressMicrofilm").succeeded()
  }

  @Test
  fun `verify task runs successfully`() {
    val project = androidAppProject()
    val result = project.build(":app:verifyMicrofilm")

    assertThat(result).task(":app:verifyMicrofilm").succeeded()
  }

  @Test
  fun `plugin does not register source set tasks without android plugin`() {
    val project = vanillaProject()
    val result = project.build("tasks", "--all", "--group=microfilm")

    assertThat(result.output).contains("compressMicrofilm")
    assertThat(result.output).contains("verifyMicrofilm")
    assertThat(result.output).doesNotContain("compressMicrofilmMain")
  }

  @Test
  fun `plugin is compatible with configuration cache`() {
    val project = androidAppProject()

    // First build stores the configuration cache
    val result1 = project.build(":app:compressMicrofilm", "--configuration-cache")
    assertThat(result1.output).contains("Configuration cache entry stored")

    // Second build reuses the configuration cache
    val result2 = project.build(":app:compressMicrofilm", "--configuration-cache")
    assertThat(result2.output).contains("Configuration cache entry reused")
  }
}
