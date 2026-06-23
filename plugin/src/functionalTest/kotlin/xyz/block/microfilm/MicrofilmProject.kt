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

import com.autonomousapps.kit.AbstractGradleProject
import com.autonomousapps.kit.GradleProject
import com.autonomousapps.kit.gradle.GradleProperties
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
      .enableIsolatedProjects()
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
      .enableIsolatedProjects()
      .write()
  }

  fun vanilla(microfilmPlugin: Plugin): GradleProject {
    return newGradleProjectBuilder(GradleProject.DslKind.KOTLIN)
      .withRootProject { withBuildScript { plugins(microfilmPlugin) } }
      .enableIsolatedProjects()
      .write()
  }

  /** Enables Gradle's Isolated Projects feature for the generated build under test. */
  private fun GradleProject.Builder.enableIsolatedProjects(): GradleProject.Builder =
    withRootProject {
      gradleProperties += GradleProperties.enableIsolatedProjects()
    }
}
