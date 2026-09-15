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
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

include(":compiler", ":compiler-tests", ":gradle-plugin", ":runtime")
