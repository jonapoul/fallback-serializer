import dev.detekt.gradle.Detekt

plugins {
  `java-gradle-plugin`
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.detekt)
}

val javaFile = layout.projectDirectory.file("../.java-version")
val jdkVersion = providers.fileContents(javaFile).asText.map { it.trim().toInt() }

kotlin {
  jvmToolchain(jdkVersion.get())
  compilerOptions {
    allWarningsAsErrors.set(true)
  }
}

detekt {
  config.from(file("../config/detekt.yml"))
  source.from("**.kts", "**.kt")
  buildUponDefaultConfig = true
}

val detektCheck =
  tasks.register("detektCheck") {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    dependsOn(tasks.withType(Detekt::class))
  }

tasks.check.configure { dependsOn(detektCheck) }

dependencies {
  fun compileOnlyPlugin(plugin: Provider<PluginDependency>) =
    compileOnly(
      plugin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version.requiredVersion}" }
    )

  compileOnly(kotlin("gradle-plugin"))
  compileOnlyPlugin(libs.plugins.detekt)
  compileOnlyPlugin(libs.plugins.licensee)

  detektPlugins(libs.detektGradle)
}

tasks.validatePlugins {
  enableStricterValidation = true
  failOnWarning = true
}

gradlePlugin.plugins.register("fallback.convention") {
  id = name
  implementationClass = "fallback.gradle.Convention"
}
