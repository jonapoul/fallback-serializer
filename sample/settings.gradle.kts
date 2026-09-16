rootProject.name = "sample"

pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositories { mavenCentral() }
  versionCatalogs { register("libs") { from(files("../gradle/libs.versions.toml")) } }
}

// Provides the Gradle plugin, and swaps in the main build's compiler and runtime projects for the
// coordinates the plugin adds
includeBuild("..")

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
