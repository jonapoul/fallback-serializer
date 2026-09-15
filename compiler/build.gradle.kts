plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.buildconfig)
  alias(libs.plugins.publish)
  id("fallback.convention")
}

kotlin {
  compilerOptions {
    optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
  }
}

buildConfig {
  packageName("fallback.serializer.compiler")
  useKotlinOutput {
    internalVisibility = true
    topLevelConstants = true
  }
  buildConfigField("String", "PLUGIN_ID", providers.gradleProperty("PLUGIN_ID").map { "\"$it\"" })
}

dependencies {
  compileOnly(kotlin("compiler"))
  compileOnly(kotlin("stdlib"))
}
