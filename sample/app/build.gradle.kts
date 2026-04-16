plugins {
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.kotlinCompose)
  id("app.cash.microfilm")
}

android {
  namespace = "app.cash.microfilm.sample.app"
  compileSdk = 36
  defaultConfig {
    applicationId = "app.cash.microfilm.sample"
    minSdk = 24
    targetSdk = 36
  }
}

dependencies {
  implementation(libs.androidActivityCompose)
  implementation(libs.composeMaterial)
  implementation(libs.composeUi)
  implementation(projects.sample.library)
}
