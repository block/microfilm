enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "microfilm"

include(":cwebp", ":plugin", ":sample:app", ":sample:library")

includeBuild("build-logic") {
  dependencySubstitution {
    substitute(module("xyz.block.microfilm:plugin")).using(project(":plugin"))
  }
}
