package xyz.block.microfilm

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
abstract class ExtractCwebpBinary : TransformAction<None> {
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
