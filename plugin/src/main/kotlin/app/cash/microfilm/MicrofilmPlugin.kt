package app.cash.microfilm

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

class MicrofilmPlugin : Plugin<Project> {
  override fun apply(target: Project): Unit = target.run {
    // Configure the extension and set the default values
    val extension = extensions.create("microfilm", MicrofilmExtension::class.java)
    extension.lossless.convention(true)
    extension.quality.convention(90)

    // Register the tasks
    val compress =
      tasks.register("compressMicrofilm") { task ->
        task.description = "Compresses source images and updates the manifest for all source sets"
        task.group = "microfilm"
      }
    val verify =
      tasks.register("verifyMicrofilm") { task ->
        task.description = "Verifies that the manifest is up to date for all source sets"
        task.group = "microfilm"
      }

    // Configure the tasks to run for each source set
    plugins.withId("com.android.application") {
      configureSourceSets(compress = compress, verify = verify)
    }
    plugins.withId("com.android.library") {
      configureSourceSets(compress = compress, verify = verify)
    }

    // Link the verify task to the common check task
    plugins.withId("base") { tasks.named("check").configure { it.dependsOn(verify) } }
  }

  private fun Project.configureSourceSets(compress: TaskProvider<*>, verify: TaskProvider<*>) {
    extensions.getByType(CommonExtension::class.java).sourceSets.configureEach { sourceSet ->
      val name = sourceSet.name
      val nameCapitalized = name.replaceFirstChar { it.uppercase() }

      // Register subtasks for each source set
      val compressSourceSet =
        tasks.register("compressMicrofilm$nameCapitalized", CompressTask::class.java) { task ->
          task.description = "Compresses source images for the '$name' source set"
          task.group = "microfilm"
        }
      val verifySourceSet =
        tasks.register("verifyMicrofilm$nameCapitalized", VerifyTask::class.java) { task ->
          task.description = "Verifies that the manifest is up to date for the '$name' source set"
          task.group = "microfilm"
        }

      // Link the subtasks to the parent tasks
      compress.configure { it.dependsOn(compressSourceSet) }
      verify.configure { it.dependsOn(verifySourceSet) }
    }
  }
}
