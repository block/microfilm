plugins {
  alias(libs.plugins.androidLibrary)
  id("xyz.block.microfilm")
}

android {
  namespace = "xyz.block.microfilm.sample.library"
  compileSdk = 36
  defaultConfig { minSdk = 24 }
}
