plugins {
  `java-gradle-plugin`
  alias(libs.plugins.kotlinJvm)
  alias(libs.plugins.testkit)
}

gradlePlugin {
  plugins {
    create("microfilm") {
      id = "app.cash.microfilm"
      implementationClass = "app.cash.microfilm.MicrofilmPlugin"
    }
  }
}

gradleTestKitSupport {
  withSupportLibrary()
  withTruthLibrary()
}

tasks.withType<Test>().configureEach {
  useJUnitPlatform()
  systemProperty("agpVersion", libs.versions.agp.get())
}

dependencies {
  compileOnly(libs.agp)

  functionalTestImplementation(platform(libs.junit.bom))
  functionalTestImplementation(libs.junit.jupiter)
  functionalTestRuntimeOnly(libs.junit.launcher)
}
