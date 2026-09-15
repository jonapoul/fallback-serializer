plugins {
  `java-gradle-plugin`
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.buildconfig)
  alias(libs.plugins.publish)
  id("fallback.convention")
}

buildConfig {
  packageName("fallback.serializer.gradle")
  useKotlinOutput {
    internalVisibility = true
    topLevelConstants = true
  }
  buildConfigField("String", "GROUP", providers.gradleProperty("GROUP").map { "\"$it\"" })
  buildConfigField("String", "PLUGIN_ID", providers.gradleProperty("PLUGIN_ID").map { "\"$it\"" })
  buildConfigField("String", "VERSION", providers.gradleProperty("VERSION_NAME").map { "\"$it\"" })
}

dependencies {
  compileOnly(kotlin("gradle-plugin-api"))
  compileOnly(kotlin("stdlib"))
}

tasks.validatePlugins {
  enableStricterValidation = true
  failOnWarning = true
}

gradlePlugin {
  plugins {
    register("fallbackSerializer") {
      id = providers.gradleProperty("PLUGIN_ID").get()
      implementationClass = "fallback.serializer.gradle.FallbackSerializerGradlePlugin"
    }
  }
}
