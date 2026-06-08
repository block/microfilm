import com.vanniktech.maven.publish.JavadocJar

plugins {
  `java-gradle-plugin`
  alias(libs.plugins.buildconfig)
  alias(libs.plugins.kotlinJvm)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.mavenPublish)
  alias(libs.plugins.testkit)
}

gradlePlugin {
  plugins {
    create("microfilm") {
      id = "xyz.block.microfilm"
      implementationClass = "xyz.block.microfilm.MicrofilmPlugin"
    }
  }
}

gradleTestKitSupport {
  disablePublication()
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

  testImplementation(libs.assertk)
  testImplementation(platform(libs.junit.bom))
  testImplementation(libs.junit.jupiter)
  testRuntimeOnly(libs.junit.launcher)

  functionalTestImplementation(platform(libs.junit.bom))
  functionalTestImplementation(libs.junit.jupiter)
  functionalTestRuntimeOnly(libs.junit.launcher)
}

buildConfig {
  useKotlinOutput { internalVisibility = true }

  packageName("xyz.block.microfilm")
  buildConfigField("String", "microfilmVersion", "\"${project.version}\"")
}

@Suppress("UnstableApiUsage")
mavenPublishing { configureBasedOnAppliedPlugins(javadocJar = JavadocJar.Empty()) }
