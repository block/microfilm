package app.cash.microfilm

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.type.ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE
import org.gradle.api.artifacts.type.ArtifactTypeDefinition.JAR_TYPE
import org.gradle.api.attributes.Usage
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.TaskProvider
import org.gradle.nativeplatform.MachineArchitecture
import org.gradle.nativeplatform.MachineArchitecture.ARCHITECTURE_ATTRIBUTE
import org.gradle.nativeplatform.MachineArchitecture.ARM64
import org.gradle.nativeplatform.MachineArchitecture.X86_64
import org.gradle.nativeplatform.OperatingSystemFamily
import org.gradle.nativeplatform.OperatingSystemFamily.LINUX
import org.gradle.nativeplatform.OperatingSystemFamily.MACOS
import org.gradle.nativeplatform.OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE

class MicrofilmPlugin : Plugin<Project> {
  override fun apply(target: Project): Unit = target.run {
    // Configure the extension and set the default values
    val extension = extensions.create("microfilm", MicrofilmExtension::class.java)
    extension.lossless.convention(true)
    extension.quality.convention(90)

    // Create a resolvable configuration for the platform-specific cwebp JAR
    val cwebpConfiguration =
      configurations.create("microfilmCwebp") { configuration ->
        configuration.isCanBeConsumed = false
        configuration.isCanBeResolved = true
        configuration.attributes { attrs ->
          attrs.attribute(
            Usage.USAGE_ATTRIBUTE,
            objects.named(Usage::class.java, Usage.NATIVE_RUNTIME),
          )
          attrs.attribute(
            OPERATING_SYSTEM_ATTRIBUTE,
            objects.named(OperatingSystemFamily::class.java, currentOperatingSystemFamily()),
          )
          attrs.attribute(
            ARCHITECTURE_ATTRIBUTE,
            objects.named(MachineArchitecture::class.java, currentMachineArchitecture()),
          )
        }
      }
    dependencies.add(
      cwebpConfiguration.name,
      "app.cash.microfilm:cwebp:${BuildConfig.microfilmVersion}",
    )

    // Register an artifact transform to extract the cwebp binary from its JAR
    dependencies.registerTransform(ExtractCwebpBinary::class.java) { spec ->
      spec.from.attribute(ARTIFACT_TYPE_ATTRIBUTE, JAR_TYPE)
      spec.to.attribute(ARTIFACT_TYPE_ATTRIBUTE, CWEBP_BINARY_TYPE)
    }

    // Create an artifact view that triggers the transform, producing the extracted cwebp binary
    val cwebpDirectory =
      cwebpConfiguration.incoming
        .artifactView { view ->
          view.attributes { it.attribute(ARTIFACT_TYPE_ATTRIBUTE, CWEBP_BINARY_TYPE) }
        }
        .artifacts
        .artifactFiles

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
      configureSourceSets(
        extension = extension,
        cwebpDirectory = cwebpDirectory,
        compress = compress,
        verify = verify,
      )
    }
    plugins.withId("com.android.library") {
      configureSourceSets(
        extension = extension,
        cwebpDirectory = cwebpDirectory,
        compress = compress,
        verify = verify,
      )
    }

    // Link the verify task to the common check task
    plugins.withId("base") { tasks.named("check").configure { it.dependsOn(verify) } }
  }

  private fun Project.configureSourceSets(
    extension: MicrofilmExtension,
    cwebpDirectory: FileCollection,
    compress: TaskProvider<*>,
    verify: TaskProvider<*>,
  ) {
    extensions.getByType(CommonExtension::class.java).sourceSets.configureEach { sourceSet ->
      val name = sourceSet.name
      val nameCapitalized = name.replaceFirstChar { it.uppercase() }

      // Register subtasks for each source set
      val compressSourceSet =
        tasks.register("compressMicrofilm$nameCapitalized", CompressTask::class.java) { task ->
          task.description = "Compresses source images for the '$name' source set"
          task.group = "microfilm"
          task.cwebpDirectory.from(cwebpDirectory)
          task.microfilmDirectory.set(layout.projectDirectory.dir("src/$name/microfilm"))
          task.resourcesDirectory.set(layout.projectDirectory.dir("src/$name/res"))
          task.lossless.set(extension.lossless)
          task.quality.set(extension.quality)
          task.outputs.upToDateWhen { false }
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

  companion object {
    private const val CWEBP_BINARY_TYPE = "cwebp-binary"
  }
}

private fun currentOperatingSystemFamily(): String {
  val operatingSystem = System.getProperty("os.name").lowercase()
  return when {
    operatingSystem.contains("mac") -> MACOS
    operatingSystem.contains("linux") -> LINUX
    else -> error("Unsupported operating system: $operatingSystem")
  }
}

private fun currentMachineArchitecture(): String {
  return when (System.getProperty("os.arch").lowercase()) {
    "aarch64" -> ARM64
    "amd64",
    "x86_64" -> X86_64
    else -> error("Unsupported machine architecture")
  }
}
