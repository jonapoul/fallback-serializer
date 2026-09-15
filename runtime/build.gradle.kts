@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.publish)
  id("fallback.convention")
}

kotlin {
  // Every target kotlinx.serialization supports, apart from ones deprecated in Kotlin
  androidNativeArm32()
  androidNativeArm64()
  androidNativeX64()
  androidNativeX86()
  iosArm64()
  iosSimulatorArm64()
  iosX64()
  js { nodejs() }
  jvm()
  linuxArm64()
  linuxX64()
  macosArm64()
  mingwX64()
  tvosArm64()
  tvosSimulatorArm64()
  wasmJs { nodejs() }
  wasmWasi { nodejs() }
  watchosArm32()
  watchosArm64()
  watchosDeviceArm64()
  watchosSimulatorArm64()

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
