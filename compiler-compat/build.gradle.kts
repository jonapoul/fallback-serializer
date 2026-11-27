import org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2

plugins {
  alias(libs.plugins.kotlin.jvm)
  id("fallback.convention")
}

kotlin {
  compilerOptions {
    optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
    // The plugin runs on the compiler's own stdlib, which can be as old as the oldest supported
    // Kotlin version
    apiVersion.set(KOTLIN_2_2)
  }
}

dependencies {
  compileOnly(kotlin("compiler"))
  compileOnly(kotlin("stdlib"))
}
