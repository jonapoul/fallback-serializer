pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

// Kotlin dev builds are only published to JetBrains' bootstrap repository
if (providers.gradleProperty("fallback.testKotlinVersion").orNull.orEmpty().contains("-dev-")) {
  listOf(
      settings.pluginManagement.repositories,
      settings.dependencyResolutionManagement.repositories,
    )
    .forEach { repositories ->
      repositories.maven("https://redirector.kotlinlang.org/maven/bootstrap") {
        content { includeGroupByRegex("org\\.jetbrains\\.kotlin(\\..*)?") }
      }
    }
}
