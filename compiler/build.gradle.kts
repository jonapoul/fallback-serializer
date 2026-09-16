plugins {
  alias(libs.plugins.publish)
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.buildconfig)
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
  buildConfigField("PLUGIN_ID", providers.gradleProperty("PLUGIN_ID"))
}

dependencies {
  compileOnly(kotlin("compiler"))
  compileOnly(kotlin("stdlib"))
}
