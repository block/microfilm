package xyz.block.microfilm

import com.autonomousapps.kit.AbstractGradleProject
import com.autonomousapps.kit.GradleProject
import com.autonomousapps.kit.gradle.Plugin
import com.autonomousapps.kit.gradle.android.AndroidBlock
import com.autonomousapps.kit.gradle.android.DefaultConfig

class MicrofilmProject : AbstractGradleProject() {

  fun androidApp(
    androidAppPlugin: Plugin,
    microfilmPlugin: Plugin,
    additions: String = "",
  ): GradleProject {
    return newGradleProjectBuilder(GradleProject.DslKind.KOTLIN)
      .withAndroidSubproject("app") {
        withBuildScript {
          plugins(androidAppPlugin, microfilmPlugin)
          android =
            AndroidBlock(
              namespace = "com.example.test",
              compileSdkVersion = 36,
              defaultConfig =
                DefaultConfig(
                  applicationId = "com.example.test",
                  minSdkVersion = 24,
                  targetSdkVersion = 36,
                  versionCode = 1,
                  versionName = "1.0",
                ),
            )
          this.additions = additions
        }
      }
      .write()
  }

  fun androidLib(
    androidLibPlugin: Plugin,
    microfilmPlugin: Plugin,
    additions: String = "",
  ): GradleProject {
    return newGradleProjectBuilder(GradleProject.DslKind.KOTLIN)
      .withSubproject("lib") {
        withBuildScript {
          plugins(androidLibPlugin, microfilmPlugin)
          this.additions =
            """
            android {
              namespace = "com.example.testlib"
              compileSdk = 36
              defaultConfig {
                minSdk = 24
              }
            }
          """
              .trimIndent() + if (additions.isNotEmpty()) "\n$additions" else ""
        }
      }
      .write()
  }

  fun vanilla(microfilmPlugin: Plugin): GradleProject {
    return newGradleProjectBuilder(GradleProject.DslKind.KOTLIN)
      .withRootProject { withBuildScript { plugins(microfilmPlugin) } }
      .write()
  }
}
