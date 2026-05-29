import com.diffplug.gradle.spotless.SpotlessExtension

buildscript { dependencies { classpath("xyz.block.microfilm:plugin") } }

plugins {
  alias(libs.plugins.androidApplication) apply false
  alias(libs.plugins.androidLibrary) apply false
  alias(libs.plugins.dokka)
  alias(libs.plugins.kotlinCompose) apply false
  alias(libs.plugins.kotlinJvm) apply false
  alias(libs.plugins.mavenPublish) apply false
  alias(libs.plugins.mavenPublishBase) apply false
  alias(libs.plugins.spotless)
}

dependencies { dokka(projects.plugin) }

configure<SpotlessExtension> {
  kotlin {
    target("**/src/**/*.kt")
    ktfmt(libs.ktfmt.get().version).googleStyle()
    licenseHeaderFile(file("gradle/license-header.txt"))
  }
  kotlinGradle {
    target("**/*.gradle.kts")
    ktfmt(libs.ktfmt.get().version).googleStyle()
  }
}

subprojects { version = extra["VERSION_NAME"]!! }
