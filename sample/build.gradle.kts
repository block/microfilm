plugins {
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.kotlinCompose)
  id("xyz.block.microfilm")
}

android {
  namespace = "xyz.block.microfilm.sample"
  compileSdk = 36
  defaultConfig {
    applicationId = "xyz.block.microfilm.sample"
    minSdk = 24
    targetSdk = 36
  }
}

dependencies {
  implementation(libs.androidActivityCompose)
  implementation(libs.composeMaterial)
  implementation(libs.composeUi)
}

microfilm {
  compress { lossless = true }

  compress("**/*_lossy.png") {
    lossless = false
    compressionFactor = 100
    compressionMethod = 6
  }

  exclude("**/*_excluded.png")
}
