plugins {
  `java-gradle-plugin`
  alias(libs.plugins.kotlinJvm)
}

gradlePlugin {
  plugins {
    create("microfilm") {
      id = "app.cash.microfilm"
      implementationClass = "app.cash.microfilm.MicrofilmPlugin"
    }
  }
}

dependencies {
  compileOnly(libs.agp)
}
