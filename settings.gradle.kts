rootProject.name = "fallback-serializer"

apply(from = "gradle/repositories.gradle.kts")

pluginManagement {
  includeBuild("build-logic")

  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins {
  id("com.gradle.develocity") version "4.5.1"
  id("io.github.gmazzo.publications.report") version "1.4.1"
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

develocity.buildScan {
  if (!gradle.startParameter.isBuildScan) {
    publishing.onlyIf { it.isAuthenticated }
  }

  uploadInBackground = false
}

include(":compiler", ":compiler-tests", ":gradle-plugin", ":runtime")
