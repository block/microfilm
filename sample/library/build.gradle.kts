plugins {
  alias(libs.plugins.androidLibrary)
  id("app.cash.microfilm")
}

android {
  namespace = "app.cash.microfilm.sample.library"
  compileSdk = 36
  defaultConfig { minSdk = 24 }
}
