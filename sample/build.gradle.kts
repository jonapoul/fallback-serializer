@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension
import org.jetbrains.kotlin.gradle.targets.wasm.yarn.WasmYarnPlugin
import org.jetbrains.kotlin.gradle.targets.wasm.yarn.WasmYarnRootExtension

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.straitjacket)
  id("dev.jonpoulton.fallbackserializer")
}

val javaFile = layout.projectDirectory.file("../.java-version")
val jdkVersion = providers.fileContents(javaFile).asText.map { it.trim().toInt() }

// One target per backend, all of which run on a Linux CI machine
kotlin {
  jvmToolchain { languageVersion.set(jdkVersion.map(JavaLanguageVersion::of)) }

  jvm()
  js { nodejs() }
  wasmJs { nodejs() }
  linuxX64()

  compilerOptions {
    allWarningsAsErrors.set(true)
    freeCompilerArgs.add("-Xexpect-actual-classes")
  }

  sourceSets {
    commonMain.dependencies { implementation(libs.kotlinx.serialization.json) }
    commonTest.dependencies { implementation(kotlin("test")) }
  }
}

// Other Kotlin versions resolve different JS tooling, so they get their own lock files
val catalog = providers.fileContents(layout.projectDirectory.file("../gradle/libs.versions.toml"))

if ("kotlin = \"${libs.versions.kotlin.get()}\"" !in catalog.asText.get()) {
  val lockDir =
    layout.buildDirectory.dir("kotlin-js-store/${libs.versions.kotlin.get()}").get().asFile
  plugins.withType<YarnPlugin> { the<YarnRootExtension>().lockFileDirectory = lockDir }
  plugins.withType<WasmYarnPlugin> {
    the<WasmYarnRootExtension>().lockFileDirectory = lockDir.resolve("wasm")
  }
}
