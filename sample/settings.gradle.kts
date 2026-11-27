rootProject.name = "sample"

apply(from = "../gradle/repositories.gradle.kts")

dependencyResolutionManagement {
  versionCatalogs {
    register("libs") {
      from(files("../gradle/libs.versions.toml"))
      // Pass -Pfallback.testKotlinVersion to build the sample with another Kotlin version
      providers.gradleProperty("fallback.testKotlinVersion").orNull?.let { kotlin ->
        version("kotlin", kotlin)
        // Kotlin 2.3 can't read kotlinx.serialization 1.12.0 and later on non-JVM targets
        if (kotlin.startsWith("2.3.")) version("kotlinx-serialization", "1.10.0")
      }
    }
  }
}

// Provides the Gradle plugin, and swaps in the main build's compiler and runtime projects for the
// coordinates the plugin adds
includeBuild("..")

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
