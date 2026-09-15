@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  id("dev.jonpoulton.fallback-serializer")
}

val javaFile = layout.projectDirectory.file("../.java-version")
val jdkVersion = providers.fileContents(javaFile).asText.map { it.trim().toInt() }

// One target per backend, all of which run on a Linux CI machine
kotlin {
  jvmToolchain(jdkVersion.get())

  jvm()
  js { nodejs() }
  wasmJs { nodejs() }
  linuxX64()

  compilerOptions { allWarningsAsErrors.set(true) }

  sourceSets {
    commonMain.dependencies { implementation(libs.kotlinx.serialization.json) }
    commonTest.dependencies { implementation(kotlin("test")) }
  }
}
