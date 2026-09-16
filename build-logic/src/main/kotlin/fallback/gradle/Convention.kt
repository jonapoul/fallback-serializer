@file:OptIn(ExperimentalAbiValidation::class)
@file:Suppress("UnstableApiUsage")

package fallback.gradle

import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import dev.detekt.gradle.plugin.DetektPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP
import org.jetbrains.kotlin.gradle.dsl.HasConfigurableKotlinCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

class Convention : Plugin<Project> {
  override fun apply(target: Project) {
    // Matches the published coordinates, so a build that includes this one (like sample) gets these
    // projects in place of the dependencies the Gradle plugin adds
    target.group = target.providers.gradleProperty("GROUP").get()
    target.version = target.providers.gradleProperty("VERSION_NAME").get()

    target.configureDetekt()

    listOf("org.jetbrains.kotlin.jvm", "org.jetbrains.kotlin.multiplatform").forEach { id ->
      target.pluginManager.withPlugin(id) { target.configureKotlin() }
    }

    target.pluginManager.withPlugin("com.vanniktech.maven.publish") {
      target.extensions.configure(KotlinProjectExtension::class.java) { e ->
        e.explicitApi()
        e.abiValidation()
      }
    }
  }

  private fun Project.configureKotlin() {
    val javaFile = isolated.rootProject.projectDirectory.file(".java-version")
    val jdkVersion = providers.fileContents(javaFile).asText.map { it.trim().toInt() }

    extensions.configure(KotlinBaseExtension::class.java) { e ->
      e.jvmToolchain(jdkVersion.get())
    }

    extensions.configure(HasConfigurableKotlinCompilerOptions::class.java) { e ->
      e.compilerOptions {
        allWarningsAsErrors.set(true)
        freeCompilerArgs.addAll("-Xcontext-sensitive-resolution")
      }
    }

    tasks.register("compileAll") { t ->
      t.dependsOn(tasks.withType(KotlinCompilationTask::class.java))
    }

    tasks.withType(Test::class.java).configureEach { t -> t.useJUnitPlatform() }
  }

  private fun Project.configureDetekt() {
    pluginManager.apply(DetektPlugin::class.java)

    extensions.configure(DetektExtension::class.java) { e ->
      e.config.from(isolated.rootProject.projectDirectory.file("config/detekt.yml"))
      e.baseline.set(file("detekt-baseline.xml"))
      e.buildUponDefaultConfig.set(true)
      e.allRules.set(true)
      e.parallel.set(true)
      e.debug.set(false)
    }

    val detektTasks = tasks.withType(Detekt::class.java)

    tasks.register("detektCheck") { t ->
      t.group = VERIFICATION_GROUP
      t.dependsOn(detektTasks)
    }

    detektTasks.configureEach { t ->
      t.reports { r ->
        r.html.required.set(true)
        r.sarif.required.set(false)
        r.checkstyle.required.set(false)
        r.markdown.required.set(false)
      }

      // Skip buildconfig output
      t.exclude { node ->
        !node.isDirectory && node.file.absolutePath.contains("generated", ignoreCase = true)
      }
    }
  }
}
