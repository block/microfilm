import com.diffplug.gradle.spotless.SpotlessExtension

buildscript { dependencies { classpath("app.cash.microfilm:plugin") } }

plugins {
  alias(libs.plugins.androidApplication) apply false
  alias(libs.plugins.androidLibrary) apply false
  alias(libs.plugins.kotlinCompose) apply false
  alias(libs.plugins.kotlinJvm) apply false
  alias(libs.plugins.spotless)
}

configure<SpotlessExtension> {
  kotlin {
    target("**/src/**/*.kt")
    ktfmt(libs.ktfmt.get().version).googleStyle()
  }
  kotlinGradle {
    target("**/*.gradle.kts")
    ktfmt(libs.ktfmt.get().version).googleStyle()
  }
}

subprojects { version = extra["VERSION_NAME"]!! }
