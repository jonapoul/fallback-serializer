rootProject.name = "sample"

pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositories { mavenCentral() }
  versionCatalogs {
    register("libs") {
      from(files("../gradle/libs.versions.toml"))
      // Pass -Pfallback.testKotlinVersion to build the sample with another Kotlin version
      providers.gradleProperty("fallback.testKotlinVersion").orNull?.let { version("kotlin", it) }
    }
  }
}

// Provides the Gradle plugin, and swaps in the main build's compiler and runtime projects for the
// coordinates the plugin adds
includeBuild("..")

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
