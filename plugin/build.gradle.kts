plugins {
  `java-gradle-plugin`
  alias(libs.plugins.kotlinJvm)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.testkit)
  alias(libs.plugins.buildconfig)
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

  dependsOn(":cwebp:publishAllPublicationsToFunctionalTestRepository")
}

dependencies {
  compileOnly(libs.agp)

  implementation(libs.kotlinx.serialization.json)

  functionalTestImplementation(platform(libs.junit.bom))
  functionalTestImplementation(libs.junit.jupiter)
  functionalTestRuntimeOnly(libs.junit.launcher)
}

buildConfig {
  useKotlinOutput {
    internalVisibility = true
  }

  packageName("app.cash.microfilm")
  buildConfigField("String", "microfilmVersion", "\"${project.version}\"")
}
