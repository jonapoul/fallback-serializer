@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.publish)
  id("fallback.convention")
}

kotlin {
  jvm()
  js { nodejs() }
  wasmJs { nodejs() }
  iosArm64()
  iosSimulatorArm64()
  linuxArm64()
  linuxX64()
  macosArm64()
  mingwX64()

  sourceSets {
    commonMain.dependencies {
      api(kotlin("stdlib"))
      api(libs.kotlinx.serialization.core)
    }

    commonTest.dependencies {
      implementation(kotlin("test"))
      implementation(libs.kotlinx.serialization.json)
    }
  }
}
