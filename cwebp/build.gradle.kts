import java.net.URI
import java.security.MessageDigest
import javax.inject.Inject
import org.gradle.nativeplatform.MachineArchitecture
import org.gradle.nativeplatform.MachineArchitecture.ARCHITECTURE_ATTRIBUTE
import org.gradle.nativeplatform.MachineArchitecture.ARM64
import org.gradle.nativeplatform.MachineArchitecture.X86_64
import org.gradle.nativeplatform.OperatingSystemFamily
import org.gradle.nativeplatform.OperatingSystemFamily.LINUX
import org.gradle.nativeplatform.OperatingSystemFamily.MACOS
import org.gradle.nativeplatform.OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE
import org.gradle.process.ExecOperations

plugins {
  base
  `maven-publish`
}

group = "app.cash.microfilm"

enum class CwebpPlatform(
  val osFamily: String,
  val architecture: String,
  val archivePlatform: String,
  val sha256: String,
) {
  MacosArm64(
    osFamily = MACOS,
    architecture = ARM64,
    archivePlatform = "mac-arm64",
    sha256 = "bc6bf84cc70f3f8574fba797d1e4a7dea4feebe9fa4be919f202413ea2b3b8f2",
  ),
  MacosX86_64(
    osFamily = MACOS,
    architecture = X86_64,
    archivePlatform = "mac-x86-64",
    sha256 = "f112dd83b420ab2a4b27d46610d9827ddf4200216023281de378647ecca31c2a",
  ),
  LinuxArm64(
    osFamily = LINUX,
    architecture = ARM64,
    archivePlatform = "linux-aarch64",
    sha256 = "69f5eebe203e0f3942fe37986209a1725741be19c152950a4283b376c95ec798",
  ),
  LinuxX86_64(
    osFamily = LINUX,
    architecture = X86_64,
    archivePlatform = "linux-x86-64",
    sha256 = "1c5ffab71efecefa0e3c23516c3a3a1dccb45cc310ae1095c6f14ae268e38067",
  )
}

val CwebpPlatform.archiveName
  get() = "libwebp-${libs.versions.cwebp.get()}-$archivePlatform.tar.gz"

val CwebpPlatform.binaryPath
  get() = "libwebp-${libs.versions.cwebp.get()}-$archivePlatform/bin/cwebp"

val CwebpPlatform.downloadUrl
  get() = "https://storage.googleapis.com/downloads.webmproject.org/releases/webp/$archiveName"

fun File.sha256Hex(): String {
  val digest = MessageDigest.getInstance("SHA-256")
  inputStream().use { input ->
    val buffer = ByteArray(8192)
    var bytesRead: Int
    while (input.read(buffer).also { bytesRead = it } != -1) {
      digest.update(buffer, 0, bytesRead)
    }
  }
  return digest.digest().joinToString("") { "%02x".format(it) }
}

fun TaskContainer.registerDownloadTask(platform: CwebpPlatform): TaskProvider<Task> =
  register("download$platform") {
    // Register the expected SHA as an input
    inputs.property("sha256", platform.sha256)

    // Register the archive file as an output
    val archive = layout.buildDirectory.file("archives/${platform.archiveName}")
    outputs.file(archive)

    doLast {
      // Download the archive file
      val archiveFile = archive.get().asFile.apply { parentFile.mkdirs() }
      val url = URI(platform.downloadUrl).toURL()
      logger.lifecycle("Downloading ${platform.archiveName}...")
      url.openStream().use { inputStream ->
        archiveFile.outputStream().use { outputStream ->
          inputStream.copyTo(outputStream)
        }
      }

      // Verify the archive file SHA
      val sha256 = archiveFile.sha256Hex()
      if (sha256 != platform.sha256) {
        archiveFile.delete()
        error(
          """
          |SHA-256 mismatch for ${platform.archiveName}:
          |  expected: ${platform.sha256}
          |  actual:   $sha256
          |The download may be corrupt or tampered with.
          """.trimMargin()
        )
      }
    }
  }

// ExecOperations must be injected via Gradle's service injection in Gradle 9+
abstract class Exec @Inject constructor(val ops: ExecOperations)

val exec = objects.newInstance<Exec>()

fun TaskContainer.registerExtractTask(
  platform: CwebpPlatform,
  download: TaskProvider<Task>,
): TaskProvider<Task> =
  register("extract$platform") {
    dependsOn(download)

    // Register the archive file as an input
    val archive = download.map { it.outputs.files.singleFile }
    inputs.file(archive)

    // Register the binary file as an output
    val binary = layout.buildDirectory.file("binaries/$platform/cwebp")
    outputs.file(binary)

    doLast {
      // Extract the binary from the archive
      val archiveFile = archive.get()
      val binaryFile = binary.get().asFile.apply { parentFile.mkdirs() }
      exec.ops.exec {
        commandLine(
          "tar",
          "xzf",
          archiveFile.absolutePath,
          "-C",
          binaryFile.parentFile.absolutePath,
          "--strip-components",
          platform.binaryPath.count { it == '/' }.toString(),
          platform.binaryPath,
        )
      }

      // Set the binary as executable
      binaryFile.setExecutable(true)
    }
  }

fun TaskContainer.registerJarTask(
  platform: CwebpPlatform,
  extract: TaskProvider<Task>,
): TaskProvider<Jar> =
  register<Jar>("jar$platform") {
    dependsOn(extract)

    // Add the binary to the JAR
    from(layout.buildDirectory.dir("binaries/$platform"))

    // Set the platform-specific classifier for the JAR
    archiveClassifier.set(platform.name)
  }

val cwebpBinaryConfiguration = configurations.create("cwebpBinary") {
  isCanBeConsumed = true
  isCanBeResolved = false

  attributes {
    attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.NATIVE_RUNTIME))
  }

  CwebpPlatform.entries.forEach { platform ->
    val variant = outgoing.variants.create(platform.toString()) {
      attributes {
        attribute(
          OPERATING_SYSTEM_ATTRIBUTE,
          objects.named(OperatingSystemFamily::class.java, platform.osFamily),
        )
        attribute(
          ARCHITECTURE_ATTRIBUTE,
          objects.named(MachineArchitecture::class.java, platform.architecture),
        )
      }
    }

    val download = tasks.registerDownloadTask(platform = platform)
    val extract = tasks.registerExtractTask(platform = platform, download = download)
    val jar = tasks.registerJarTask(platform = platform, extract = extract)

    variant.artifact(jar)
  }
}

val softwareComponentFactory = extensions.getByType(PublishingExtension::class).softwareComponentFactory
val cwebpComponent = softwareComponentFactory.adhoc("cwebpComponent")
components.add(cwebpComponent)

cwebpComponent.addVariantsFromConfiguration(cwebpBinaryConfiguration) {
  mapToMavenScope("runtime")
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["cwebpComponent"])
    }
  }
}
