import org.gradle.api.attributes.Category.CATEGORY_ATTRIBUTE
import org.gradle.api.attributes.Category.LIBRARY
import org.gradle.api.attributes.LibraryElements.JAR
import org.gradle.api.attributes.LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE
import org.gradle.api.attributes.Usage.JAVA_RUNTIME
import org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2

plugins {
  alias(libs.plugins.publish)
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.buildconfig)
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

buildConfig {
  packageName("fallback.serializer.compiler")
  useKotlinOutput {
    internalVisibility = true
    topLevelConstants = true
  }
  buildConfigField("PLUGIN_ID", providers.gradleProperty("PLUGIN_ID"))
}

// The compat modules, which are bundled into this jar rather than published
val compat = configurations.dependencyScope("compat")

val compatClasspath =
  configurations.resolvable("compatClasspath") {
    extendsFrom(compat)
    isTransitive = false
    attributes {
      attribute(CATEGORY_ATTRIBUTE, objects.named(LIBRARY))
      attribute(USAGE_ATTRIBUTE, objects.named(JAVA_RUNTIME))
      attribute(LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(JAR))
    }
  }

dependencies {
  compileOnly(kotlin("compiler"))
  compileOnly(kotlin("stdlib"))

  listOf(":compiler-compat", ":compiler-compat:k2220", ":compiler-compat:k240").forEach { path ->
    compileOnly(project(path))
    compat(project(path))
  }
}

tasks.jar {
  from(compatClasspath.map { files -> files.map(::zipTree) }) { exclude("META-INF/MANIFEST.MF") }
}
