import com.github.burrunan.s3cache.AwsS3BuildCache

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
  id("com.github.burrunan.s3-build-cache") version "1.9.11"
  id("com.gradle.develocity") version "4.6.1"
  id("io.github.gmazzo.publications.report") version "1.4.2"
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

val url = providers.environmentVariable("S3_BUILD_CACHE_ENDPOINT").filter(String::isNotBlank)
val cacheBucket = providers.environmentVariable("S3_BUILD_CACHE_BUCKET").filter(String::isNotBlank)

if (url.isPresent && cacheBucket.isPresent) {
  logger.lifecycle("Remote caching enabled")
  buildCache.remote<AwsS3BuildCache> {
    endpoint = url.get()
    bucket = cacheBucket.get()
    region = "auto"
    forcePathStyle = true
    isReducedRedundancy = false
    isPush = true
  }
} else {
  logger.warn("Remote caching disabled!")
}

develocity.buildScan {
  if (!gradle.startParameter.isBuildScan) {
    publishing.onlyIf { it.isAuthenticated }
  }

  uploadInBackground = false
}

include(
  ":compiler",
  ":compiler-compat",
  ":compiler-compat:k2220",
  ":compiler-compat:k240",
  ":compiler-tests",
  ":gradle-plugin",
  ":runtime",
)
