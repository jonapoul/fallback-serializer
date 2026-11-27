@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_3
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.publish)
  id("fallback.convention")
}

kotlin {
  // So consumers on the oldest supported Kotlin version can read the runtime
  compilerOptions {
    languageVersion.set(KOTLIN_2_3)
    apiVersion.set(KOTLIN_2_3)
  }
  targets.configureEach {
    if (platformType != KotlinPlatformType.jvm && platformType != KotlinPlatformType.common) {
      compilations.configureEach {
        compileTaskProvider.configure {
          compilerOptions.freeCompilerArgs.addAll(
            "-Xklib-abi-version=2.3.0",
            "-Xklib-ir-inliner=disabled",
          )
        }
      }
    }
  }

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
      // Not published, since consumers on JS and Wasm need the stdlib to match their own compiler
      compileOnly(kotlin("stdlib"))
      api(libs.kotlinx.serialization.floor)
    }

    commonTest.dependencies {
      implementation(kotlin("stdlib"))
      implementation(kotlin("test"))
      implementation(libs.kotlinx.serialization.cbor)
      implementation(libs.kotlinx.serialization.json)
      implementation(libs.kotlinx.serialization.protobuf)
      implementation(libs.xmlutil)
    }
  }
}
